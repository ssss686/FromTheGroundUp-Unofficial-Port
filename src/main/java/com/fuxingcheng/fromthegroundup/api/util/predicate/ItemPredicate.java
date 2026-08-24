package com.fuxingcheng.fromthegroundup.api.util.predicate;

import java.util.function.Function;

import com.google.gson.JsonObject;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemPredicate {

	private final Ingredient ingredient;

	protected ItemPredicate() {
		this.ingredient = null;
	}

	public ItemPredicate(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	public ItemPredicate(ItemStack... stacks) {
		this.ingredient = Ingredient.of(stacks);
	}

	public boolean test(ItemStack stack) {
		return ingredient.test(stack);
	}

	public ItemStack[] getMatchingStacks() {
		return ingredient.getItems();
	}

	public ItemStack getDisplayStack() {
		ItemStack[] matching = ingredient.getItems();
		if (matching.length > 0)
			return matching[0];
		return ItemStack.EMPTY;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public static ItemPredicate getAsPredicate(Ingredient ingredient) {
		return new ItemPredicate(ingredient);
	}

	public interface Factory extends Function<JsonObject, ItemPredicate> {
	}

}
