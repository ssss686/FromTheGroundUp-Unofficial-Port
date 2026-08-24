package com.fuxingcheng.fromthegroundup.mixin;

import com.fuxingcheng.fromthegroundup.util.CraftingPlayerContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class MixinCraftingMenu {

	@Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"))
	private static void onSlotChangedCraftingGridHead(AbstractContainerMenu menu, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer, RecipeHolder<?> recipe, CallbackInfo ci) {
		CraftingPlayerContext.setPlayer(player);
	}

	@Inject(method = "slotChangedCraftingGrid", at = @At("RETURN"))
	private static void onSlotChangedCraftingGridReturn(AbstractContainerMenu menu, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer, RecipeHolder<?> recipe, CallbackInfo ci) {
		CraftingPlayerContext.clear();
	}
}
