package ftgumod;

import ftgumod.event.PlayerLockEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.NeoForge;

public class CraftingListener implements ContainerListener {

	private final Player player;

	CraftingListener(Player player) {
		this.player = player;
	}

	@Override
	public void slotChanged(AbstractContainerMenu menu, int index, ItemStack stack) {
		if (stack.isEmpty())
			return;

		Slot slot = menu.getSlot(index);
		if (slot != null && slot.container instanceof ResultContainer resultContainer) {
			RecipeHolder<?> recipe = resultContainer.getRecipeUsed();
			PlayerLockEvent event = new PlayerLockEvent(player, stack, recipe);
			NeoForge.EVENT_BUS.post(event);

			if (!event.isCanceled()) {
				slot.container.setItem(0, ItemStack.EMPTY);
				if (player instanceof ServerPlayer)
					Content.c_itemLocked.trigger((ServerPlayer) player, recipe, stack);
			}
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu menu, int property, int value) {
	}

}
