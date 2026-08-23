package com.Fuxingcheng.ftgumod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerInspectEvent extends PlayerEvent {

	private final InteractionHand hand;
	private final BlockPos pos;
	private final BlockState state;
	private final Direction face;
	private boolean canceled;

	public PlayerInspectEvent(Player player, InteractionHand hand, BlockPos pos, BlockState state, Direction face) {
		super(player);
		this.hand = hand;
		this.pos = pos;
		this.state = state;
		this.face = face;
		this.canceled = false;
	}

	public InteractionHand getHand() {
		return hand;
	}

	public BlockPos getPos() {
		return pos;
	}

	public BlockState getState() {
		return state;
	}

	public Direction getFace() {
		return face;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}