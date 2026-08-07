package ftgumod.packet.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import ftgumod.FTGU;
import ftgumod.api.event.FTGUClientSyncEvent;
import ftgumod.api.technology.ITechnology;
import ftgumod.client.gui.toast.ToastTechnology;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TechnologyMessage(Collection<String> tech, boolean force, ITechnology[] toasts) implements CustomPacketPayload {

	public static final Type<TechnologyMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "technology"));

	public static final StreamCodec<FriendlyByteBuf, TechnologyMessage> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public TechnologyMessage decode(FriendlyByteBuf buf) {
			boolean force = buf.readBoolean();
			int size = buf.readVarInt();
			Collection<String> tech = new HashSet<>();
			for (int i = 0; i < size; i++)
				tech.add(buf.readUtf());
			int toastCount = buf.readVarInt();
			ITechnology[] toasts = new ITechnology[toastCount];
			for (int i = 0; i < toastCount; i++)
				toasts[i] = TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(buf.readUtf()));
			return new TechnologyMessage(tech, force, toasts);
		}

		@Override
		public void encode(FriendlyByteBuf buf, TechnologyMessage msg) {
			buf.writeBoolean(msg.force());
			if (msg.tech() != null) {
				buf.writeVarInt(msg.tech().size());
				for (String s : msg.tech())
					buf.writeUtf(s);
			} else {
				buf.writeVarInt(0);
			}
			buf.writeVarInt(msg.toasts().length);
			for (ITechnology toast : msg.toasts())
				buf.writeUtf(toast.getRegistryName().toString());
		}
	};

	public TechnologyMessage(Player player, boolean force, ITechnology... toasts) {
		this(getTech(player), force, toasts);
	}

	private static Collection<String> getTech(Player player) {
		CapabilityTechnology.ITechnology cap = player.getData(CapabilityTechnology.TECH_CAP.get());
		if (cap != null)
			return new HashSet<>(cap.getResearched());
		throw new IllegalArgumentException();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(TechnologyMessage message, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			CapabilityTechnology.ITechnology cap = player.getData(CapabilityTechnology.TECH_CAP.get());
			if (cap == null)
				return;

			if (!message.force() && cap.getResearched().size() == message.tech().size())
				return;

			Collection<String> researched = new ArrayList<>(cap.getResearched());
			for (String name : researched)
				if (!message.tech().contains(name)) {
					cap.removeResearched(name);
					String[] split = name.split("#");
					if (split.length == 2) {
						Technology tech = TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(split[0]));
						if (tech != null)
							TechnologyManager.INSTANCE.getProgress(player, tech).revokeCriterion(split[1]);
					}
				}

			for (String name : message.tech())
				if (!cap.isResearched(name)) {
					cap.setResearched(name);
					String[] split = name.split("#");
					if (split.length == 2) {
						Technology tech = TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(split[0]));
						if (tech != null)
							TechnologyManager.INSTANCE.getProgress(player, tech).grantCriterion(split[1]);
					}
				}

			for (ITechnology toast : message.toasts())
				Minecraft.getInstance().getToasts().addToast(new ToastTechnology(toast));

			NeoForge.EVENT_BUS.post(new FTGUClientSyncEvent.Post());
		});
	}

}
