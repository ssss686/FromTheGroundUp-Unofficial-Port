package com.Fuxingcheng.ftgumod.api.technology.unlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.Fuxingcheng.ftgumod.api.FTGUAPI;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.server.ServerLifecycleHooks;

public class UnlockRecipe implements IUnlock {

	private final Ingredient recipe;
	@Nullable
	private final Set<ResourceLocation> recipeTypes;

	/**
	 * getRecipeList() 的结果缓存：配方在两次数据包重载之间是静态的，只有模组加载或 /reload 会变。
	 * 不用监听事件来清缓存 —— /reload 时 MinecraftServer 会换掉 RecipeManager，
	 * 紧接着 updateRegistryTags() 发出 TagsUpdatedEvent，FTGU 在那里重建全部 Technology，
	 * 也就是缓存所在的对象自己就被丢掉了。
	 */
	@Nullable
	private List<RecipeHolder<?>> cachedRecipes;

	public UnlockRecipe(Ingredient recipe) {
		this(recipe, null);
	}

	public UnlockRecipe(Ingredient recipe, @Nullable Set<ResourceLocation> recipeTypes) {
		this.recipe = recipe;
		this.recipeTypes = recipeTypes;
	}

	@Override
	public boolean isDisplayed() {
		return recipe.getItems().length > 0;
	}

	@Override
	public Ingredient getIcon() {
		return recipe;
	}

	@Override
	public boolean unlocks(ItemStack stack) {
		return recipe.test(stack);
	}

	public Collection<RecipeHolder<?>> getRecipeList() {
		if (cachedRecipes == null) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			RecipeManager manager = server.getRecipeManager();
			RegistryAccess access = server.registryAccess();
			List<RecipeHolder<?>> recipes = new ArrayList<>();
			for (RecipeHolder<?> holder : manager.getRecipes()) {
				if (recipeTypes != null) {
					// RecipeType 只是个接口，拿它本身和 ResourceLocation 比永远不相等，必须反查注册表拿注册名。
					// 没注册进 RECIPE_TYPE 的匿名实现反查不到 id，按“不在白名单里”处理。
					ResourceLocation type = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
					if (type == null || !recipeTypes.contains(type))
						continue;
				}
				if (unlocks(holder.value().getResultItem(access)))
					recipes.add(holder);
			}
			cachedRecipes = List.copyOf(recipes);
		}
		// 返回副本：调用方拿到的和以前一样是个可以随便改的列表，缓存本身不会被改坏
		return new ArrayList<>(cachedRecipes);
	}

	@Override
	public void unlock(ServerPlayer player) {
		FTGUAPI.technologyManager.addRecipes(getRecipeList(), player);
	}

	@Override
	public void lock(ServerPlayer player) {
		for (RecipeHolder<?> holder : getRecipeList())
			player.getRecipeBook().remove(holder);
	}

}
