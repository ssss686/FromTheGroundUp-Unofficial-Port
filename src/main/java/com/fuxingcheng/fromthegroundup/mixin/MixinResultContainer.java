package com.fuxingcheng.fromthegroundup.mixin;

import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import com.fuxingcheng.fromthegroundup.util.CraftingPlayerContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultContainer.class)
public class MixinResultContainer {

	@Unique
	private RecipeHolder<?> ftgumod$currentRecipe;

	@Inject(method = "setRecipeUsed", at = @At("HEAD"))
	private void onSetRecipeUsed(RecipeHolder<?> recipe, CallbackInfo ci) {
		ftgumod$currentRecipe = recipe;
	}

	@Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
	private void onSetItem(int index, ItemStack stack, CallbackInfo ci) {
		if (index == 0 && !stack.isEmpty()) {
			Player player = CraftingPlayerContext.getPlayer();
			if (player != null && TechnologyManager.INSTANCE.isLocked(stack, player)) {
				// Trigger the advancement before cancelling
				if (player instanceof ServerPlayer serverPlayer) {
					Content.c_itemLocked.trigger(serverPlayer, ftgumod$currentRecipe, stack);
				}
				ci.cancel();
			}
		}
	}
}
