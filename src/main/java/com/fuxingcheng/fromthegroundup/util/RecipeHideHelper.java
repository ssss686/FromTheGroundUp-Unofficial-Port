package com.fuxingcheng.fromthegroundup.util;

import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Recipe hide utility
 * Encapsulates recipe book cleanup logic
 */
public class RecipeHideHelper {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Clean locked recipes from recipe book
	 * Uses ServerRecipeBook.removeRecipes() to ensure client receives REMOVE packet
	 *
	 * @param player server player
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
			// removeRecipes sends REMOVE network packet to client
			player.getRecipeBook().removeRecipes(lockedRecipes, player);
			LOGGER.info("RecipeHideHelper: removed {} locked recipes for player {}", lockedRecipes.size(), player.getName().getString());
		}
	}

}
