package com.fuxingcheng.fromthegroundup.mixin;

import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class MixinCraftingMenu {

	@Unique
	private static final ThreadLocal<Player> currentPlayer = new ThreadLocal<>();

	@Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"))
	private static void onSlotChangedCraftingGridHead(CraftingMenu menu, Player player, CallbackInfo ci) {
		currentPlayer.set(player);
	}

	@Inject(method = "slotChangedCraftingGrid", at = @At("RETURN"))
	private static void onSlotChangedCraftingGridReturn(CraftingMenu menu, Player player, CallbackInfo ci) {
		currentPlayer.remove();
	}

	public static Player getCurrentPlayer() {
		return currentPlayer.get();
	}
}
