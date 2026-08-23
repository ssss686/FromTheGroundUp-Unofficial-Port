package com.Fuxingcheng.ftgumod.criterion;

import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.Fuxingcheng.ftgumod.FTGU;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class TriggerInspect extends TriggerFTGU<TriggerInspect.Instance> {

	public TriggerInspect(String id) {
		super(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, id));
	}

	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player, BlockPos pos, BlockState state, boolean success) {
		PlayerAdvancements advancements = player.getAdvancements();
		Set<Listener<Instance>> set = listeners.get(advancements);
		if (set != null) {
			ServerLevel world = player.serverLevel();
			for (Listener<Instance> listener : set)
				if (listener.trigger().test(world, pos, state, success))
					listener.run(advancements);
		}
	}

	public record Instance(Optional<ResourceLocation> block, Optional<Boolean> success) implements CriterionTriggerInstance {

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.optionalFieldOf("block").forGetter(Instance::block),
				Codec.BOOL.optionalFieldOf("success").forGetter(Instance::success)
		).apply(instance, Instance::new));

		public boolean test(ServerLevel world, BlockPos pos, BlockState state, boolean success) {
			return (block.isEmpty() || block.get().equals(BuiltInRegistries.BLOCK.getKey(state.getBlock())))
					&& (this.success.isEmpty() || this.success.get() == success);
		}

		@Override
		public void validate(CriterionValidator validator) {
		}

	}

}
