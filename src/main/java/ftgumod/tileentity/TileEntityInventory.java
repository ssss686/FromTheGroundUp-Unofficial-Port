package ftgumod.tileentity;

import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;

public abstract class TileEntityInventory extends BaseContainerBlockEntity {

	private NonNullList<ItemStack> stacks;
	private final String name;

	public TileEntityInventory(BlockEntityType<?> type, BlockPos pos, BlockState state, int size, String name) {
		super(type, pos, state);
		this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
		this.name = name;
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);
		this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, this.stacks, registries);
	}

	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);
		ContainerHelper.saveAllItems(compound, this.stacks, registries);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		this.stacks.clear();
	}

	@Override
	public int getContainerSize() {
		return stacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : stacks)
			if (!stack.isEmpty())
				return false;
		return true;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return stacks;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.stacks = items;
	}

	@Override
	protected Component getDefaultName() {
		return Component.literal(name);
	}

	@Override
	protected abstract AbstractContainerMenu createMenu(int containerId, Inventory inventory);

}
