package ftgumod.tileentity;

import ftgumod.FTGU;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;

public abstract class TileEntityInventory extends BaseContainerBlockEntity implements Container {

	private final ItemStack[] stack;
	private final String name;

	public TileEntityInventory(BlockEntityType<?> type, BlockPos pos, BlockState state, int size, String name) {
		super(type, pos, state);
		this.stack = new ItemStack[size];
		this.name = name;
		clearContent();
	}

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.loadAdditional(compound, registries);

		ListTag items = compound.getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < items.size(); ++i) {
			CompoundTag tag = items.getCompound(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < stack.length)
				stack[slot] = ItemStack.parseOptional(registries, tag);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
		super.saveAdditional(compound, registries);

		ListTag items = new ListTag();
		for (int i = 0; i < stack.length; ++i) {
			if (!stack[i].isEmpty()) {
				CompoundTag nbtTagCompound = new CompoundTag();
				nbtTagCompound.putByte("Slot", (byte) i);
				stack[i].save(registries, nbtTagCompound);
				items.add(nbtTagCompound);
			}
		}
		compound.put("Items", items);
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < stack.length; i++)
			stack[i] = ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int arg0, int arg1) {
		if (!stack[arg0].isEmpty()) {
			ItemStack itemstack;

			if (stack[arg0].getCount() <= arg1) {
				itemstack = stack[arg0];
				stack[arg0] = ItemStack.EMPTY;
				return itemstack;
			} else {
				itemstack = stack[arg0].split(arg1);

				if (stack[arg0].getCount() == 0) {
					stack[arg0] = ItemStack.EMPTY;
				}

				return itemstack;
			}
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public int getContainerSize() {
		return stack.length;
	}

	@Override
	public ItemStack getItem(int arg0) {
		return stack[arg0];
	}

	@Override
	public boolean canPlaceItem(int arg0, ItemStack arg1) {
		return true;
	}

	@Override
	public boolean stillValid(Player arg0) {
		return true;
	}

	@Override
	public ItemStack removeItemNoUpdate(int arg0) {
		ItemStack item = stack[arg0].copy();
		stack[arg0] = ItemStack.EMPTY;
		return item;
	}

	@Override
	public void setItem(int arg0, ItemStack arg1) {
		stack[arg0] = arg1;

		if (!arg1.isEmpty() && arg1.getCount() > getMaxStackSize()) {
			arg1.setCount(getMaxStackSize());
		}
	}

	@Override
	public void setItems(NonNullList<ItemStack> items) {
		for (int i = 0; i < items.size() && i < stack.length; i++)
			stack[i] = items.get(i);
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return NonNullList.of(ItemStack.EMPTY, stack);
	}

	@Override
	public Component getDefaultName() {
		return Component.literal(name);
	}

	@Override
	public Component getName() {
		return Component.literal(name);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack i : stack)
			if (!i.isEmpty())
				return false;
		return true;
	}

}
