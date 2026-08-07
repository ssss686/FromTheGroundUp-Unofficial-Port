package ftgumod.api.util.predicate;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class ItemFluid extends ItemPredicate {

	private final FluidStack fluid;

	public ItemFluid(FluidStack fluid) {
		super(FluidUtil.getFilledBucket(fluid));
		this.fluid = fluid;
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return FluidUtil.getFluidContained(itemStack)
				.map(stack -> stack.is(fluid.getFluid()))
				.orElse(false);
	}

	public static class Factory implements ItemPredicate.Factory {

		@Override
		public ItemPredicate apply(JsonObject json) {
			String fluidName = GsonHelper.getAsString(json, "fluid");
			int count = GsonHelper.getAsInt(json, "count", 1000);
			Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidName));
			if (fluid == BuiltInRegistries.FLUID.get(BuiltInRegistries.FLUID.getDefaultKey()) && !"minecraft:empty".equals(fluidName))
				throw new JsonSyntaxException("Unknown fluid: " + fluidName);
			return new ItemFluid(new FluidStack(fluid, count));
		}

	}

}
