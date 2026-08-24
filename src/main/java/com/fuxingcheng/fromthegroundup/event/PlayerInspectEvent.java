package com.fuxingcheng.fromthegroundup.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class PlayerInspectEvent {

	private final Player player;
	private final InteractionHand hand;
	private final Direction face;
	private final BlockPos pos;
	private final BlockState block;
	private boolean canceled;

	public static final Event<Consumer> EVENT = EventFactory.createArrayBacked(Consumer.class,
			listeners -> event -> {
				for (Consumer listener : listeners) {
					listener.accept(event);
					if (event.isCanceled()) {
						break;
					}
				}
			});

	public PlayerInspectEvent(Player player, InteractionHand hand, BlockPos pos, BlockState block, Direction face) {
		this.player = player;
		this.hand = hand;
		this.pos = pos;
		this.block = block;
		this.face = face;
	}

	public Player getPlayer() {
		return player;
	}

	public InteractionHand getHand() {
		return hand;
	}

	public Direction getFace() {
		return face;
	}

	public BlockPos getBlockPos() {
		return pos;
	}

	public Level getLevel() {
		return player.level();
	}

	public BlockState getBlockState() {
		return block;
	}

	public Block getBlock() {
		return block.getBlock();
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public interface Consumer {
		void accept(PlayerInspectEvent event);
	}

}
