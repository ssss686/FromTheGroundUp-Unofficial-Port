package com.fuxingcheng.fromthegroundup.api.technology.unlock;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.NonNullList;

public class UnlockCompound implements IUnlock {

	private final NonNullList<IUnlock> list;
	private final boolean display;
	private final Ingredient icon;

	public UnlockCompound(NonNullList<IUnlock> list) {
		this.list = list;
		List<IUnlock> stream = list.stream().filter(IUnlock::isDisplayed).collect(Collectors.toList());
		display = stream.size() > 0;
		if (display) {
			ItemStack[] stacks = stream.stream()
					.flatMap(u -> java.util.Arrays.stream(u.getIcon().getItems()))
					.toArray(ItemStack[]::new);
			icon = Ingredient.of(stacks);
		} else {
			icon = null;
		}
	}

	@Override
	public boolean isDisplayed() {
		return display;
	}

	@Override
	public Ingredient getIcon() {
		return icon;
	}

	@Override
	public boolean unlocks(ItemStack stack) {
		return list.stream().anyMatch(unlock -> unlock.unlocks(stack));
	}

	@Override
	public void unlock(ServerPlayer player) {
		list.forEach(unlock -> unlock.unlock(player));
	}

	@Override
	public void lock(ServerPlayer player) {
		list.forEach(unlock -> unlock.lock(player));
	}

}
