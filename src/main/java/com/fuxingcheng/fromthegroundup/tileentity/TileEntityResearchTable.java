package com.fuxingcheng.fromthegroundup.tileentity;

import javax.annotation.Nullable;

import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.api.technology.recipe.IPuzzle;
import com.fuxingcheng.fromthegroundup.inventory.ContainerResearchTable;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.util.StackUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityResearchTable extends TileEntityInventory {

	public IPuzzle puzzle;

	public TileEntityResearchTable(BlockPos pos, BlockState state) {
		super(Content.te_researchTable, pos, state, 12, Content.n_researchTable);
	}

	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);
		if (puzzle != null)
			compound.put("Puzzle", puzzle.write(registries));
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);
		if (puzzle == null) {
			Technology tech = StackUtils.INSTANCE.getTechnology(getItem(1));
			if (tech != null && tech.hasResearchRecipe())
				puzzle = tech.getResearchRecipe().createInstance();
		}
		if (puzzle != null && compound.contains("Puzzle"))
			puzzle.read(compound.get("Puzzle"), registries);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		saveAdditional(tag, registries);
		return tag;
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInv) {
		return new ContainerResearchTable(containerId, this, playerInv);
	}

}
