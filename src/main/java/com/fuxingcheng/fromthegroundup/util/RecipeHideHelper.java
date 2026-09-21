package com.fuxingcheng.fromthegroundup.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import com.fuxingcheng.fromthegroundup.technology.CapabilityTechnology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 配方隐藏工具类
 * 封装配方书清理逻辑，以及切石机那份不走配方书的配方列表
 */
public class RecipeHideHelper {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * 上一轮过滤过的切石机。
	 * key 是菜单实例（WeakHashMap，列表不引用菜单，不会把菜单吊住），value 是当时的关键输入。
	 */
	private static final Map<AbstractContainerMenu, StonecutterFilter> STONECUTTER = Collections
			.synchronizedMap(new WeakHashMap<>());

	private record StonecutterFilter(List<RecipeHolder<StonecutterRecipe>> list, Collection<String> researched,
			Map<String, ?> technologies) {
	}

	/**
	 * 清理配方书中锁定的配方
	 * 使用 ServerRecipeBook.removeRecipes() 确保客户端收到 REMOVE 包
	 *
	 * @param player 服务端玩家
	 */
	public static void cleanRecipeBook(ServerPlayer player) {
		List<RecipeHolder<?>> lockedRecipes = new ArrayList<>();
		for (RecipeHolder<?> holder : player.serverLevel().getRecipeManager().getRecipes()) {
			ItemStack result = holder.value().getResultItem(player.serverLevel().registryAccess());
			if (TechnologyManager.INSTANCE.isLocked(result, player)) {
				lockedRecipes.add(holder);
			}
		}

		if (!lockedRecipes.isEmpty()) {
			// removeRecipes 会发送 REMOVE 网络包给客户端
			player.getRecipeBook().removeRecipes(lockedRecipes, player);
			LOGGER.info("RecipeHideHelper: removed {} locked recipes for player {}", lockedRecipes.size(), player.getName().getString());
		}
	}

	/**
	 * 把切石机左边那排"能切出什么"里结果还没解锁的配方摘掉。
	 *
	 * 高炉、熔炉那些走的是配方书（AbstractFurnaceMenu 是 RecipeBookMenu），配方书由
	 * {@link #cleanRecipeBook} 清；切石机不吃这套 —— 它的列表是现查的（StonecutterMenu.setupRecipeList
	 * 里的 getRecipesFor），只看输入物、不看玩家，所以锁着的结果照样列在左边。
	 *
	 * 两边都要过滤，而且过滤后两边顺序必须一模一样：切石机只把选中的下标同步给服务端
	 * （selectedRecipeIndex），点第几个格子发的就是过滤后列表里的下标，服务端拿同一套顺序取配方。
	 * 好在两边的列表都来自各自的 RecipeManager.getRecipesFor，按结果的翻译键排序（原版本来也
	 * 依赖两边一致），这里不改变相对顺序，只删，所以下标对得上。
	 *
	 * 原版那份列表是原地删的（getRecipes() 返回的就是字段本身，改得动），删完就分不出"过滤过的"
	 * 和"原版刚查的"了，所以缓存上一轮的关键输入：列表实例没换、玩家已研究的科技没变、技术定义
	 * 没重新加载，就不再重算 —— 客户端每帧都要调这里，重算一趟得把每条配方过一遍 isLocked。
	 *
	 * @param menu   已经打开的菜单（不是切石机就直接返回）
	 * @param player 这个菜单的主人；客户端传本地玩家
	 */
	public static void filterStonecutterRecipes(@Nullable AbstractContainerMenu menu, @Nullable Player player) {
		if (player == null || !(menu instanceof StonecutterMenu stonecutter))
			return;

		List<RecipeHolder<StonecutterRecipe>> recipes = stonecutter.getRecipes();
		// 空列表没什么可过滤的：原版只在输入物不是空的时候才去查列表，查出来的也是新的一份
		if (recipes.isEmpty())
			return;

		CapabilityTechnology.ITechnology cap = player.getAttachedOrCreate(CapabilityTechnology.TECH_CAP);
		Collection<String> researched = cap.getResearched();
		Map<String, ?> technologies = TechnologyManager.INSTANCE.cache;

		StonecutterFilter last = STONECUTTER.get(menu);
		if (last != null && last.list() == recipes && last.researched().equals(researched)
				&& last.technologies() == technologies)
			return;

		RegistryAccess registries = player.level().registryAccess();
		List<RecipeHolder<StonecutterRecipe>> locked = new ArrayList<>();
		for (RecipeHolder<StonecutterRecipe> holder : recipes)
			if (TechnologyManager.INSTANCE.isLocked(holder.value().getResultItem(registries), player))
				locked.add(holder);

		// 记的是过滤后的那个列表实例，所以要在删之前存
		STONECUTTER.put(menu, new StonecutterFilter(recipes, new HashSet<>(researched), technologies));

		if (!locked.isEmpty()) {
			recipes.removeAll(locked);
			LOGGER.debug("RecipeHideHelper: hid {} locked stonecutting recipes for player {}", locked.size(),
					player.getName().getString());
		}
	}

}
