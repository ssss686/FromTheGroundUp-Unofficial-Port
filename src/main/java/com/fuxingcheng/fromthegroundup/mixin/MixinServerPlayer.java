package com.fuxingcheng.fromthegroundup.mixin;

import com.fuxingcheng.fromthegroundup.CraftingListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {

	@Inject(method = "openMenu", at = @At("RETURN"))
	private void onOpenMenu(MenuProvider factory, CallbackInfoReturnable<OptionalInt> cir) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		AbstractContainerMenu menu = player.containerMenu;
		if (menu != null) {
			menu.addSlotListener(new CraftingListener(player));
		}
	}
}
