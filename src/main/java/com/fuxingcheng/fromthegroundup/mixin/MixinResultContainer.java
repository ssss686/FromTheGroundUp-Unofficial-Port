package com.fuxingcheng.fromthegroundup.mixin;

import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultContainer.class)
public class MixinResultContainer {

	@Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
	private void onSetItem(int index, ItemStack stack, CallbackInfo ci) {
		if (index == 0 && !stack.isEmpty()) {
			Player player = MixinCraftingMenu.getCurrentPlayer();
			if (player != null && TechnologyManager.INSTANCE.isLocked(stack, player)) {
				// Prevent setting locked items in the result slot
				ci.cancel();
			}
		}
	}
}
