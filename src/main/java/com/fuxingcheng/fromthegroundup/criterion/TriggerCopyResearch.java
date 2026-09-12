package com.fuxingcheng.fromthegroundup.criterion;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.fuxingcheng.fromthegroundup.FTGU;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.resources.ResourceLocation;

public class TriggerCopyResearch extends TriggerFTGU<TriggerCopyResearch.Instance> {

	public TriggerCopyResearch(String id) {
		super(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, id));
	}

	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, Technology technology) {
		PlayerAdvancements advancements = player.getAdvancements();
		for (Listener<Instance> listener : snapshotListeners(advancements))
			if (listener.trigger().test(technology))
				listener.run(advancements);
	}

	public record Instance(Optional<ResourceLocation> technology) implements CriterionTriggerInstance {

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.optionalFieldOf("technology").forGetter(Instance::technology)
		).apply(instance, Instance::new));

		public boolean test(Technology technology) {
			return this.technology.isEmpty() || this.technology.get().equals(technology.getRegistryName());
		}

		@Override
		public void validate(CriterionValidator validator) {
		}

	}

}
