package com.fuxingcheng.fromthegroundup.criterion;

import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.fuxingcheng.fromthegroundup.FTGU;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.ResourceLocation;

public class TriggerRecipeLocked extends TriggerFTGU<TriggerRecipeLocked.Instance> {

	public TriggerRecipeLocked(String id) {
		super(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, id));
	}

	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, RecipeHolder<?> recipe, ItemStack stack) {
		PlayerAdvancements advancements = player.getAdvancements();
		Set<Listener<Instance>> set = listeners.get(advancements);
		if (set != null)
			for (Listener<Instance> listener : set)
				if (listener.trigger().test(recipe, stack))
					listener.run(advancements);
	}

	public record Instance(Optional<ResourceLocation> recipe, Optional<ItemPredicate> item) implements CriterionTriggerInstance {

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.optionalFieldOf("recipe").forGetter(Instance::recipe),
				ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Instance::item)
		).apply(instance, Instance::new));

		public boolean test(RecipeHolder<?> recipe, ItemStack stack) {
			return (this.recipe.isEmpty() || (recipe != null && this.recipe.get().equals(recipe.id())))
					&& (this.item.isEmpty() || this.item.get().test(stack));
		}

		@Override
		public void validate(CriterionValidator validator) {
		}

	}

}
