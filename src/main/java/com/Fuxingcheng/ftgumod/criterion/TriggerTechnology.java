package com.Fuxingcheng.ftgumod.criterion;

import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.Fuxingcheng.ftgumod.FTGU;
import com.Fuxingcheng.ftgumod.packet.PacketDispatcher;
import com.Fuxingcheng.ftgumod.packet.client.TechnologyMessage;
import com.Fuxingcheng.ftgumod.technology.Technology;
import com.Fuxingcheng.ftgumod.technology.TechnologyManager;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.resources.ResourceLocation;

public class TriggerTechnology extends TriggerFTGU<TriggerTechnology.Instance> {

	public TriggerTechnology(String id) {
		super(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, id));
	}

	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, Technology technology) {
		PlayerAdvancements advancements = player.getAdvancements();
		Set<Listener<Instance>> set = listeners.get(advancements);
		if (set != null)
			for (Listener<Instance> listener : set)
				if (listener.trigger().test(technology))
					listener.run(advancements);

		Set<ListenerTech<Instance>> techSet = techListeners.get(advancements);
		if (techSet != null)
			for (ListenerTech<Instance> listenerTech : techSet)
				if (listenerTech.triggerInstance().test(technology)) {
					listenerTech.listenerTechnology().technology()
							.grantCriterion(player, listenerTech.listenerTechnology().name());
					PacketDispatcher.sendTo(new TechnologyMessage(player, true), player);
				}
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
