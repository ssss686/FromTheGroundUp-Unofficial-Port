package ftgumod.api.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class InventoryCraftingPersistent extends SimpleContainer {

	private final Container parent;
	private final int offset;
	private final int size;

	public InventoryCraftingPersistent(Container parent, int offset, int width, int height) {
		super(width * height);
		this.parent = parent;
		this.offset = offset;
		this.size = width * height;
	}

	@Override
	public ItemStack getItem(int index) {
		return index < 0 || index >= size ? ItemStack.EMPTY : parent.getItem(index + offset);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		return getItem(index).split(count);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		if (index >= 0 && index < size)
			parent.setItem(index + offset, stack);
	}

	@Override
	public void setChanged() {
		parent.setChanged();
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < size; i++)
			parent.removeItemNoUpdate(i + offset);
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < size; i++)
			if (!parent.getItem(i + offset).isEmpty())
				return false;
		return true;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return index < 0 || index >= size ? ItemStack.EMPTY : parent.removeItemNoUpdate(index + offset);
	}

}
