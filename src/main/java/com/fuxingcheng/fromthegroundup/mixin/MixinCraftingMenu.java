package com.fuxingcheng.fromthegroundup.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class MixinCraftingMenu {

	@Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"))
	private static void onSlotChangedCraftingGridHead(CraftingMenu menu, Player player, CallbackInfo ci) {
		CraftingPlayerContext.setPlayer(player);
	}

	@Inject(method = "slotChangedCraftingGrid", at = @At("RETURN"))
	private static void onSlotChangedCraftingGridReturn(CraftingMenu menu, Player player, CallbackInfo ci) {
		CraftingPlayerContext.clear();
	}
}
