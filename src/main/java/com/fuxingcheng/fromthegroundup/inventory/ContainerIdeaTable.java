package com.fuxingcheng.fromthegroundup.inventory;

import java.util.function.Predicate;

import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.api.inventory.InventoryCraftingPersistent;
import com.fuxingcheng.fromthegroundup.api.inventory.SlotSpecial;
import com.fuxingcheng.fromthegroundup.api.util.IStackUtils;
import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyMessage;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import com.fuxingcheng.fromthegroundup.tileentity.TileEntityInventory;
import com.fuxingcheng.fromthegroundup.util.StackUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ContainerIdeaTable extends AbstractContainerMenu {

	private static final TagKey<Item> FEATHERS = TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "feathers"));

	private final TileEntityInventory invInput;
	private final InventoryCraftingPersistent craftMatrix;
	private final Inventory invPlayer;

	private final int sizeInventory;

	private int feather;
	private int parchment;
	private int combine;
	private int output;

	private NonNullList<ItemStack> remaining;

	public ContainerIdeaTable(int containerId, TileEntityInventory tileEntity, Inventory invPlayer) {
		super(Content.m_ideaTable, containerId);
		this.invInput = tileEntity;
		this.invPlayer = invPlayer;
		if (!invPlayer.player.level().isClientSide())
			PacketDispatcher.sendTo(new TechnologyMessage(invPlayer.player, false), (ServerPlayer) invPlayer.player);

		sizeInventory = addSlots(tileEntity);

		for (int slotx = 0; slotx < 3; slotx++) {
			for (int sloty = 0; sloty < 9; sloty++) {
				addSlot(new Slot(invPlayer, sloty + slotx * 9 + 9, 8 + sloty * 18, 84 + slotx * 18));
			}
		}

		for (int slot = 0; slot < 9; slot++) {
			addSlot(new Slot(invPlayer, slot, 8 + slot * 18, 142));
		}

		craftMatrix = new InventoryCraftingPersistent(tileEntity, combine, 3, 1);
		slotsChanged(invInput);
	}

	private int addSlots(TileEntityInventory tileEntity) {
		int c = 0;

		addSlot(new SlotSpecial(tileEntity, c, 37, 23, 1,
				(Predicate<ItemStack>) stack -> stack.is(FEATHERS)));
		feather = c;
		c++;

		addSlot(new SlotSpecial(tileEntity, c, 59, 23, 64, new ItemStack(Content.i_parchmentEmpty)));
		parchment = c;
		c++;

		combine = c;
		for (int slot = 0; slot < 3; slot++) {
			addSlot(new SlotSpecial(tileEntity, c, 30 + slot * 18, 45, 1, (Predicate<ItemStack>) null));
			c++;
		}

		addSlot(new Slot(new SimpleContainer(1), 0, 124, 35));
		output = c;
		c++;

		return c;
	}

	private Technology hasRecipe() {
		for (Technology tech : TechnologyManager.INSTANCE) {
			if (tech.hasIdeaRecipe() && tech.canResearch(invPlayer.player)) {
				remaining = tech.getIdeaRecipe().test(craftMatrix);
				if (remaining != null)
					return tech;
			}
		}
		return null;
	}

	@Override
	public void slotsChanged(Container inv) {
		if (inv == invInput) {
			if (slots.get(feather).hasItem() && slots.get(parchment).hasItem()) {
				Technology tech = hasRecipe();

				if (tech != null) {
					slots.get(output).set(StackUtils.INSTANCE.getParchment(tech,
							tech.hasResearchRecipe() ? IStackUtils.Parchment.IDEA : IStackUtils.Parchment.RESEARCH));
					return;
				}
			}
			slots.get(output).set(ItemStack.EMPTY);
		}
	}

	@Override
	public void clicked(int index, int mouse, ClickType mode, Player player) {
		if (index == output && slots.get(output).hasItem()) {
			slots.get(parchment).remove(1);
			// Client-side container never runs hasRecipe() (slots not synced yet at
			// construction), so remaining is null there; the server handles the rest.
			if (remaining != null)
				for (int i = 0; i < craftMatrix.getContainerSize(); i++)
					craftMatrix.setItem(i, remaining.get(i));
		}

		super.clicked(index, mouse, mode, player);
		slotsChanged(invInput);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int slotIndex) {
		ItemStack itemStack1 = ItemStack.EMPTY;
		Slot slot = slots.get(slotIndex);

		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack1 = itemStack2.copy();

			if (slotIndex == output) {
				if (!moveItemStackTo(itemStack2, sizeInventory, sizeInventory + 36, true))
					return ItemStack.EMPTY;
			} else if (slotIndex > output) {
				if (itemStack2.getItem() == Content.i_parchmentEmpty) {
					if (!moveItemStackTo(itemStack2, parchment, parchment + 1, false))
						return ItemStack.EMPTY;
				} else if (itemStack2.is(FEATHERS)) {
					if (!moveItemStackTo(itemStack2, feather, feather + 1, false))
						return ItemStack.EMPTY;
				} else if (!moveItemStackTo(itemStack2, combine, combine + 3, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(itemStack2, sizeInventory, sizeInventory + 36, false))
				return ItemStack.EMPTY;

			if (itemStack2.getCount() != 0)
				slot.setChanged();

			if (itemStack2.getCount() == itemStack1.getCount())
				return ItemStack.EMPTY;

			slot.onTake(playerIn, itemStack2);
		}

		return itemStack1;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

}
