package com.fuxingcheng.fromthegroundup.api.technology.puzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.fuxingcheng.fromthegroundup.api.FTGUAPI;
import com.fuxingcheng.fromthegroundup.api.inventory.ContainerResearch;
import com.fuxingcheng.fromthegroundup.api.inventory.InventoryCraftingPersistent;
import com.fuxingcheng.fromthegroundup.api.technology.puzzle.gui.PuzzleGuiMatch;
import com.fuxingcheng.fromthegroundup.api.technology.recipe.IPuzzle;
import com.fuxingcheng.fromthegroundup.api.util.BlockSerializable;
import com.fuxingcheng.fromthegroundup.inventory.ContainerResearchTable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class PuzzleMatch implements IPuzzle {

	private final List<ContainerResearch> registry = new LinkedList<>();
	private final ResearchMatch research;
	private List<Component> hints;
	private ContainerResearchTable container;

	public PuzzleMatch(ResearchMatch research) {
		this.research = research;
	}

	@Nullable
	private Container inventory() {
		return container != null ? new InventoryCraftingPersistent(container.invInput, 3, 3, 3) : null;
	}

	@Override
	public Tag write(HolderLookup.Provider registries) {
		ListTag items = new ListTag();
		Container inv = inventory();
		if (inv == null)
			return items;
		for (int i = 0; i < 9; ++i) {
			if (!inv.getItem(i).isEmpty()) {
				CompoundTag compound = (CompoundTag) inv.getItem(i).save(registries);
				compound.putByte("Slot", (byte) i);
				items.add(compound);
			}
		}
		return items;
	}

	@Override
	public void read(Tag tag, HolderLookup.Provider registries) {
		ListTag items = (ListTag) tag;
		Container inv = inventory();
		if (inv == null)
			return;
		for (int i = 0; i < items.size(); ++i) {
			CompoundTag compound = items.getCompound(i);
			byte slot = compound.getByte("Slot");
			if (slot >= 0 && slot < 9)
				inv.setItem(slot, ItemStack.parseOptional(registries, compound));
		}
	}

	@Override
	public ResearchMatch getRecipe() {
		return research;
	}

	@Override
	public boolean test() {
		Container inv = inventory();
		if (inv == null)
			return false;
		for (int i = 0; i < 9; i++) {
			if (research.ingredients[i].test(inv.getItem(i)))
				continue;
			return false;
		}
		return true;
	}

	@Override
	public void onStart(ContainerResearch container) {
		if (!registry.contains(container))
			registry.add(container);
		this.container = (ContainerResearchTable) container;
	}

	@Override
	public void onInventoryChange(ContainerResearch container) {
		if (!container.isClient()) {
			hints = new ArrayList<>();
			List<BlockSerializable> inspected = Collections.emptyList();
			if (container.getSlot(2).hasItem())
				inspected = FTGUAPI.stackUtils.getInspected(container.getSlot(2).getItem());
			for (int i = 0; i < 9; i++)
				if (research.hasHint(i))
					hints.add(research.getHint(i).getHint(inspected));
				else
					hints.add(null);

			container.refreshHints(hints);
		}
	}

	@Override
	public void onFinish() {
		Container inv = inventory();
		if (inv == null)
			return;
		for (int i = 0; i < 9; i++) {
			if (research.consume[i] != null) {
				if (research.consume[i])
					inv.setItem(i, ItemStack.EMPTY);
				else
					inv.setItem(i, inv.getItem(i).copy());
			} else {
				ItemStack stack = inv.getItem(i);
				if (!stack.isEmpty()) {
					ItemStack remaining = stack.copy();
					remaining.shrink(1);
					inv.setItem(i, remaining);
				}
			}
		}
	}

	@Override
	public void onRemove(@Nullable Player player, Level world, BlockPos pos) {
		Container inv = inventory();
		if (inv != null) {
			if (player != null && world != null && !world.isClientSide) {
				for (int i = 0; i < 9; i++) {
					ItemStack stack = inv.getItem(i);
					if (!stack.isEmpty() && !player.addItem(stack))
						player.drop(stack, false);
				}
			} else if (world != null)
				Containers.dropContents(world, pos, inv);
		}

		registry.clear();
	}

	@Override
	public void setHints(List<Component> hints) {
		this.hints = hints;
	}

	@Override
	public List<Component> getHints() {
		return hints;
	}

	@Override
	public Object getGui() {
		return new PuzzleGuiMatch(this, inventory());
	}

}
