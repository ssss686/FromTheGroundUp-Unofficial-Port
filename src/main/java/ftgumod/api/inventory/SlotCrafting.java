package ftgumod.api.inventory;

import java.util.function.Predicate;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class SlotCrafting extends SlotSpecial {

	private final AbstractContainerMenu eventHandler;

	public SlotCrafting(AbstractContainerMenu eventHandler, Container inventory, int index, int x, int y, int limit,
			Predicate<ItemStack> special) {
		super(inventory, index, x, y, limit, special);
		this.eventHandler = eventHandler;
	}

	public SlotCrafting(AbstractContainerMenu eventHandler, Container inventory, int index, int x, int y, int limit,
			Iterable<ItemStack> special) {
		super(inventory, index, x, y, limit, special);
		this.eventHandler = eventHandler;
	}

	public SlotCrafting(AbstractContainerMenu eventHandler, Container inventory, int index, int x, int y, int limit,
			ItemStack special) {
		super(inventory, index, x, y, limit, special);
		this.eventHandler = eventHandler;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		eventHandler.slotsChanged(container);
	}

}
