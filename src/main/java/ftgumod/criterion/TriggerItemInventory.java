package ftgumod.criterion;

import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ftgumod.FTGU;
import ftgumod.api.FTGUAPI;
import ftgumod.api.util.JsonContextPublic;
import ftgumod.api.util.predicate.ItemPredicate;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.world.item.ItemStack;

public class TriggerItemInventory extends TriggerFTGU<TriggerItemInventory.Instance> {

	public static TriggerItemInventory INSTANCE;

	public TriggerItemInventory(String id) {
		super(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(FTGU.MODID, id));
		INSTANCE = this;
	}

	@Override
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public void trigger(ServerPlayer player) {
		PlayerAdvancements advancements = player.getAdvancements();
		Set<ListenerTech<Instance>> techSet = techListeners.get(advancements);
		if (techSet == null || techSet.isEmpty())
			return;

		for (ListenerTech<Instance> listenerTech : techSet) {
			Instance inst = listenerTech.triggerInstance();
			ItemPredicate pred = inst.getPredicate();
			if (pred == null)
				continue;

			for (ItemStack stack : player.getInventory().items) {
				if (!stack.isEmpty() && pred.test(stack)) {
					listenerTech.listenerTechnology().technology()
							.grantCriterion(player, listenerTech.listenerTechnology().name());
					PacketDispatcher.sendTo(new TechnologyMessage(player, true), player);
					return;
				}
			}
		}
	}

	public record Instance(Optional<String> predicate) implements CriterionTriggerInstance {

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("predicate").forGetter(Instance::predicate)
		).apply(instance, Instance::new));

		public ItemPredicate getPredicate() {
			if (predicate.isEmpty())
				return null;
			JsonObject json = new JsonObject();
			json.addProperty("type", predicate.get());
			return FTGUAPI.stackUtils.getItemPredicate(json, new JsonContextPublic(FTGU.MODID));
		}

		@Override
		public void validate(CriterionValidator validator) {
		}
	}
}
