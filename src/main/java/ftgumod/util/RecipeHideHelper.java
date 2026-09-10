package ftgumod.util;

import ftgumod.technology.TechnologyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 配方隐藏工具类
 * 封装配方书清理逻辑
 */
public class RecipeHideHelper {

	private static final Logger LOGGER = LogManager.getLogger();

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

}
