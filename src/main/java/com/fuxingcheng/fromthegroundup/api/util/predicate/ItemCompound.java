package com.fuxingcheng.fromthegroundup.api.util.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemCompound extends ItemPredicate {

	private final Collection<ItemPredicate> predicates;
	private ItemStack[] matching;

	public ItemCompound(Collection<ItemPredicate> predicates) {
		this.predicates = predicates;
	}

	@Override
	public ItemStack[] getMatchingStacks() {
		if (matching == null) {
			List<ItemStack> tmp = new ArrayList<>();
			for (ItemPredicate child : predicates)
				Collections.addAll(tmp, child.getMatchingStacks());
			matching = tmp.toArray(new ItemStack[tmp.size()]);
		}
		return matching;
	}

	@Override
	public Ingredient getIngredient() {
		return Ingredient.of(getMatchingStacks());
	}

	@Override
	public ItemStack getDisplayStack() {
		ItemStack[] matching = getMatchingStacks();
		if (matching.length > 0)
			return matching[0];
		return ItemStack.EMPTY;
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return predicates.stream().anyMatch(i -> i.test(itemStack));
	}

}
