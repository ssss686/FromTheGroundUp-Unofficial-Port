package com.Fuxingcheng.ftgumod.api.util.predicate;

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
		if (ingredient == null || ingredient.isEmpty()) return false;
		try {
			return ingredient.test(stack);
		} catch (Exception e) {
			return false;
		}
	}

	public ItemStack[] getMatchingStacks() {
		if (ingredient == null || ingredient.isEmpty()) return new ItemStack[0];
		try {
			ItemStack[] stacks = ingredient.getItems();
			return stacks != null ? stacks : new ItemStack[0];
		} catch (Exception e) {
			return new ItemStack[0];
		}
	}

	public ItemStack getDisplayStack() {
		ItemStack[] matching = getMatchingStacks();
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
