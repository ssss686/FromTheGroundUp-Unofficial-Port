package com.fuxingcheng.fromthegroundup;

import com.fuxingcheng.fromthegroundup.event.PlayerLockEvent;
import com.fuxingcheng.fromthegroundup.util.RecipeHideHelper;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CraftingListener implements ContainerListener {

	private final Player player;

	public CraftingListener(Player player) {
		this.player = player;
	}

	@Override
	public void slotChanged(AbstractContainerMenu menu, int index, ItemStack stack) {
		// 切石机左边那排"能切出什么"不走配方书，得单独摘。原版换输入物时会重新查一份列表，
		// 所以每次广播都过一遍（列表没变的话 RecipeHideHelper 里会直接跳过）
		RecipeHideHelper.filterStonecutterRecipes(menu, player);

		if (stack.isEmpty())
			return;

		Slot slot = menu.getSlot(index);
		if (slot != null && slot.container instanceof ResultContainer resultContainer) {
			RecipeHolder<?> recipe = resultContainer.getRecipeUsed();
			PlayerLockEvent event = new PlayerLockEvent(player, stack, recipe);
			PlayerLockEvent.EVENT.invoker().accept(event);

			if (!event.isCanceled()) {
				slot.container.setItem(0, ItemStack.EMPTY);
				if (player instanceof ServerPlayer) {
					Content.c_itemLocked.trigger((ServerPlayer) player, recipe, stack);
				}
			}
		}
	}

	@Override
	public void dataChanged(AbstractContainerMenu menu, int property, int value) {
	}

}
