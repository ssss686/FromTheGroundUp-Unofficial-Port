package com.fuxingcheng.fromthegroundup.api.util.predicate;

import java.util.function.Predicate;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

public class ItemLambda extends ItemPredicate {

	public static final ItemPredicate EMPTY = new ItemLambda(ItemStack::isEmpty);

	private final Predicate<ItemStack> predicate;

	public ItemLambda(Predicate<ItemStack> predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(ItemStack stack) {
		return predicate.test(stack);
	}

	public static class Factory implements ItemPredicate.Factory {

		private final ItemPredicate predicate;

		public Factory(Predicate<ItemStack> predicate) {
			this.predicate = new ItemLambda(predicate);
		}

		@Override
		public ItemPredicate apply(JsonObject jsonObject) {
			return predicate;
		}

	}

}
