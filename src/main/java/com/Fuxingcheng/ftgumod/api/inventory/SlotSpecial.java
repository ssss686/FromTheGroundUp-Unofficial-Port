package com.Fuxingcheng.ftgumod.api.inventory;

import java.util.function.Predicate;

import com.Fuxingcheng.ftgumod.api.FTGUAPI;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotSpecial extends Slot {

	private final Predicate<ItemStack> special;
	private final int limit;

	public SlotSpecial(Container inventory, int index, int x, int y, int limit, Predicate<ItemStack> special) {
		super(inventory, index, x, y);
		this.special = special;
		this.limit = limit;
	}

	public SlotSpecial(Container inventory, int index, int x, int y, int limit, Iterable<ItemStack> special) {
		this(inventory, index, x, y, limit, item -> {
			for (ItemStack s : special)
				if (FTGUAPI.stackUtils.isStackOf(s, item))
					return true;
			return false;
		});
	}

	public SlotSpecial(Container inventory, int index, int x, int y, int limit, ItemStack special) {
		this(inventory, index, x, y, limit, item -> FTGUAPI.stackUtils.isStackOf(special, item));
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return special == null || special.test(stack);
	}

	@Override
	public int getMaxStackSize() {
		return limit;
	}

}
