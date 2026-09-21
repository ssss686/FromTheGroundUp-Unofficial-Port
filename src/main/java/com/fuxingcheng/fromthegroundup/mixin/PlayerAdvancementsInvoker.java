package com.fuxingcheng.fromthegroundup.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * 给 {@code PlayerAdvancements#unregisterListeners(AdvancementHolder)} 开个口子 —— 它按
 * {@code advancement.value().criteria()} 挨条调 {@code trigger.removePlayerListener}，
 * 只动这一个进度挂上去的监听，别的进度、别的玩家一概不碰，正是"照着假进度 id 把监听摘干净"要的那条路。
 *
 * 触发器上有两个公开的替代：{@code removePlayerListener}（要现成的 Listener 对象）和
 * {@code removePlayerListeners}（一锅端，会把原版成就挂着的监听连带清掉）。前者在这里用不上 ——
 * 每 reload 一次都会重新 build 一个假 AdvancementHolder，老监听是当时那个对象，我们手上已经没有引用了。
 */
@Mixin(PlayerAdvancements.class)
public interface PlayerAdvancementsInvoker {

	@Invoker("unregisterListeners")
	void ftgu$unregisterListeners(AdvancementHolder advancement);

}
