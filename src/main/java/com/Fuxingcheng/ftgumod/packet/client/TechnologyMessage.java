package com.Fuxingcheng.ftgumod.packet.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.Fuxingcheng.ftgumod.ClientHooks;
import com.Fuxingcheng.ftgumod.api.event.FTGUClientSyncEvent;
import com.Fuxingcheng.ftgumod.api.technology.ITechnology;
import com.Fuxingcheng.ftgumod.technology.CapabilityTechnology;
import com.Fuxingcheng.ftgumod.technology.Technology;
import com.Fuxingcheng.ftgumod.technology.TechnologyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class TechnologyMessage {

	private final Collection<String> tech;
	private final boolean force;
	private final ITechnology[] toasts;

	public TechnologyMessage(Collection<String> tech, boolean force, ITechnology[] toasts) {
		this.tech = tech;
		this.force = force;
		this.toasts = toasts;
	}

	public TechnologyMessage(Player player, boolean force, ITechnology... toasts) {
		this(getTech(player), force, toasts);
	}

	private static Collection<String> getTech(Player player) {
		CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP)
				.orElse(new CapabilityTechnology.DefaultImpl());
		return new HashSet<>(cap.getResearched());
	}

	public static void encode(TechnologyMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.force);
		if (msg.tech != null) {
			buf.writeVarInt(msg.tech.size());
			for (String s : msg.tech)
				buf.writeUtf(s);
		} else {
			buf.writeVarInt(0);
		}
		buf.writeVarInt(msg.toasts.length);
		for (ITechnology toast : msg.toasts)
			buf.writeUtf(toast.getRegistryName().toString());
	}

	public static TechnologyMessage decode(FriendlyByteBuf buf) {
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

	public static void handle(TechnologyMessage message, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.getSender();
			if (player == null)
				player = net.minecraft.client.Minecraft.getInstance().player;
			if (player == null)
				return;

			CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP)
					.orElse(new CapabilityTechnology.DefaultImpl());

			if (!message.force && cap.getResearched().size() == message.tech.size())
				return;

			Collection<String> researched = new ArrayList<>(cap.getResearched());
			for (String name : researched)
				if (!message.tech.contains(name)) {
					cap.removeResearched(name);
					String[] split = name.split("#");
					if (split.length == 2) {
						Technology tech = TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(split[0]));
						if (tech != null)
							TechnologyManager.INSTANCE.getProgress(player, tech).revokeCriterion(split[1]);
					}
				}

			for (String name : message.tech)
				if (!cap.isResearched(name)) {
					cap.setResearched(name);
					String[] split = name.split("#");
					if (split.length == 2) {
						Technology tech = TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(split[0]));
						if (tech != null)
							TechnologyManager.INSTANCE.getProgress(player, tech).grantCriterion(split[1]);
					}
				}

			for (ITechnology toast : message.toasts)
				ClientHooks.displayToast.accept(toast);

			MinecraftForge.EVENT_BUS.post(new FTGUClientSyncEvent.Post());
		});
		ctx.setPacketHandled(true);
	}
}
