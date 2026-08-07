package ftgumod.event;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerInspectEvent extends PlayerEvent implements ICancellableEvent {

	private final InteractionHand hand;
	private final Direction face;
	private final BlockPos pos;
	private final BlockState block;

	public PlayerInspectEvent(Player player, InteractionHand hand, BlockPos pos, BlockState block, Direction face) {
		super(player);
		this.hand = hand;
		this.pos = pos;
		this.block = block;
		this.face = face;
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
		return getEntity().level();
	}

	public BlockState getBlockState() {
		return block;
	}

	public Block getBlock() {
		return block.getBlock();
	}

}
