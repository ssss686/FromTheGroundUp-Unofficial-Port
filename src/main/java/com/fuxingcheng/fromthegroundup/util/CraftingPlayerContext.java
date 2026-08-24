package com.fuxingcheng.fromthegroundup.util;

import net.minecraft.world.entity.player.Player;

/**
 * Thread-local storage for the current player during crafting operations.
 * Used by MixinCraftingMenu and MixinResultContainer to check locked recipes.
 */
public class CraftingPlayerContext {
	private static final ThreadLocal<Player> CURRENT_PLAYER = new ThreadLocal<>();

	public static void setPlayer(Player player) {
		CURRENT_PLAYER.set(player);
	}

	public static Player getPlayer() {
		return CURRENT_PLAYER.get();
	}

	public static void clear() {
		CURRENT_PLAYER.remove();
	}
}
