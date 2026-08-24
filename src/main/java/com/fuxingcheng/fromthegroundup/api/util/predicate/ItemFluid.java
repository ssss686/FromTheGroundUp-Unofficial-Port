package com.fuxingcheng.fromthegroundup.api.util.predicate;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ItemFluid extends ItemPredicate {

	private final Fluid fluid;
	private final int amount;

	public ItemFluid(Fluid fluid, int amount) {
		super(getBucketStack(fluid));
		this.fluid = fluid;
		this.amount = amount;
	}

	private static ItemStack getBucketStack(Fluid fluid) {
		if (fluid == Fluids.WATER) return new ItemStack(Items.WATER_BUCKET);
		if (fluid == Fluids.LAVA) return new ItemStack(Items.LAVA_BUCKET);
		return new ItemStack(Items.BUCKET);
	}

	@Override
	public boolean test(ItemStack itemStack) {
		// Simple check - in Fabric, we check if the item is a bucket of the fluid
		if (itemStack.getItem() == Items.WATER_BUCKET && fluid == Fluids.WATER) return true;
		if (itemStack.getItem() == Items.LAVA_BUCKET && fluid == Fluids.LAVA) return true;
		return false;
	}

	public static class Factory implements ItemPredicate.Factory {

		@Override
		public ItemPredicate apply(JsonObject json) {
			String fluidName = GsonHelper.getAsString(json, "fluid");
			int amount = GsonHelper.getAsInt(json, "count", 1000);
			Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidName));
			if (fluid == BuiltInRegistries.FLUID.get(BuiltInRegistries.FLUID.getDefaultKey()) && !"minecraft:empty".equals(fluidName))
				throw new JsonSyntaxException("Unknown fluid: " + fluidName);
			return new ItemFluid(fluid, amount);
		}

	}

}
