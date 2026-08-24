package com.fuxingcheng.fromthegroundup.api.inventory;

import java.util.function.Predicate;

import com.fuxingcheng.fromthegroundup.api.FTGUAPI;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotSpecial extends Slot {

	private final Predicate<ItemStack> special;
	private final int limit;

	/**
	 * 构造时传入的容器索引。原版 Slot 把它存进私有字段 slot，Mojmap 1.21.1 没有任何公开 getter
	 * （NeoForge 的 getSlotIndex() 是 NeoForge 自己补丁加的）。菜单会把 slot.index 覆盖为
	 * 菜单列表位置，所以必须自己保存这份容器索引。
	 */
	private final int containerIndex;

	public SlotSpecial(Container inventory, int index, int x, int y, int limit, Predicate<ItemStack> special) {
		super(inventory, index, x, y);
		this.special = special;
		this.limit = limit;
		this.containerIndex = index;
	}

	public int getContainerIndex() {
		return containerIndex;
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
