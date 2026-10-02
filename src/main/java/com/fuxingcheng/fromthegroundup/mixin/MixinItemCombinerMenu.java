package com.fuxingcheng.fromthegroundup.mixin;

import com.fuxingcheng.fromthegroundup.util.CraftingPlayerContext;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让锻造台/铁砧这类 ItemCombinerMenu 的"算结果"也带上玩家上下文，
 * 这样 MixinResultContainer 能在 ResultContainer#setItem 处就地把锁定结果拦下来
 * （与工作台同一条路径），而不是等广播出去再清。
 */
@Mixin(ItemCombinerMenu.class)
public class MixinItemCombinerMenu {

	@Shadow
	@Final
	protected Player player;

	@Inject(method = "slotsChanged", at = @At("HEAD"))
	private void ftgumod$setPlayerContext(Container container, CallbackInfo ci) {
		CraftingPlayerContext.setPlayer(player);
	}

	@Inject(method = "slotsChanged", at = @At("RETURN"))
	private void ftgumod$clearPlayerContext(Container container, CallbackInfo ci) {
		CraftingPlayerContext.clear();
	}
}
