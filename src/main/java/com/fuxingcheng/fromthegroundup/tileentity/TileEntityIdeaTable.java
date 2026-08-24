package com.fuxingcheng.fromthegroundup.tileentity;

import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.inventory.ContainerIdeaTable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIdeaTable extends TileEntityInventory implements ExtendedScreenHandlerFactory {

	public TileEntityIdeaTable(BlockPos pos, BlockState state) {
		super(Content.te_ideaTable, pos, state, 5, Content.n_ideaTable);
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
		return new ContainerIdeaTable(containerId, this, playerInv);
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory playerInv) {
		return new ContainerIdeaTable(containerId, this, playerInv);
	}

	@Override
	public Object getScreenOpeningData(ServerPlayer player) {
		return this.getBlockPos();
	}

}
