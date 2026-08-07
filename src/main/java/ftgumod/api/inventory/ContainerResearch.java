package ftgumod.api.inventory;

import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;

public abstract class ContainerResearch extends AbstractContainerMenu {

	protected ContainerResearch(MenuType<?> type, int containerId) {
		super(type, containerId);
	}

	@Override
	public Slot addSlot(Slot slot) {
		return super.addSlot(slot);
	}

	public void removeSlots(int size) {
		for (int i = 0; i < size; i++)
			slots.remove(slots.size() - 1);
	}

	public abstract boolean isClient();

	public abstract Player getPlayer();

	public abstract void refreshHints(List<Component> hints);

	public abstract void setChanged();

	public void update() {
		slotsChanged(null);
	}

}
