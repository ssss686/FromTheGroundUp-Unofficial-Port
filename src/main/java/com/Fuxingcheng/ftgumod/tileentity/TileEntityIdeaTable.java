package com.Fuxingcheng.ftgumod.tileentity;

import com.Fuxingcheng.ftgumod.Content;
import com.Fuxingcheng.ftgumod.inventory.ContainerIdeaTable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIdeaTable extends TileEntityInventory {

	public TileEntityIdeaTable(BlockPos pos, BlockState state) {
		super(Content.te_ideaTable.get(), pos, state, 5, Content.n_ideaTable);
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInv) {
		return new ContainerIdeaTable(containerId, this, playerInv);
	}
}