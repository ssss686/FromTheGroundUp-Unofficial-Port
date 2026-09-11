package com.fuxingcheng.fromthegroundup.packet.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.api.technology.ITechnology;
import com.fuxingcheng.fromthegroundup.technology.CapabilityTechnology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record TechnologyMessage(Collection<String> tech, boolean force, ITechnology[] toasts) implements CustomPacketPayload {

	public static final Type<TechnologyMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "technology"));

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
		CapabilityTechnology.ITechnology cap = player.getAttachedOrCreate(CapabilityTechnology.TECH_CAP);
		if (cap != null)
			return new HashSet<>(cap.getResearched());
		throw new IllegalArgumentException();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
