package com.fuxingcheng.fromthegroundup.client;

import java.util.ArrayList;
import java.util.Collection;

import com.fuxingcheng.fromthegroundup.ClientHooks;
import com.fuxingcheng.fromthegroundup.FTGUConfig;
import com.fuxingcheng.fromthegroundup.api.event.FTGUClientSyncEvent;
import com.fuxingcheng.fromthegroundup.api.technology.ITechnology;
import com.fuxingcheng.fromthegroundup.inventory.ContainerResearchTable;
import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.packet.client.HintMessage;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyInfoMessage;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyMessage;
import com.fuxingcheng.fromthegroundup.packet.server.RequestMessage;
import com.fuxingcheng.fromthegroundup.technology.CapabilityTechnology;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

/**
 * Client-side handlers for the server-to-client packets.
 * They are kept out of the payload classes on purpose: a dedicated server must be able to
 * register the payload types, and loading a class whose methods touch {@code net.minecraft.client.*}
 * types (for instance {@code ctx.player()} returning a {@code LocalPlayer}) fails there.
 */
@Environment(EnvType.CLIENT)
public final class ClientPacketHandlers {

	public static void handleTechnologyMessage(TechnologyMessage message, ClientPlayNetworking.Context ctx) {
		ctx.client().execute(() -> {
			Player player = ctx.player();
			CapabilityTechnology.ITechnology cap = player.getAttachedOrCreate(CapabilityTechnology.TECH_CAP);
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
				ClientHooks.displayToast.accept(toast);

			// Fire client sync event
			FTGUClientSyncEvent.POST.invoker().run();
		});
	}

	public static void handleTechnologyInfoMessage(TechnologyInfoMessage message, ClientPlayNetworking.Context ctx) {
		ctx.client().execute(() -> {
			FTGUConfig.cachedLoadDefaultTechnologies = message.loadDefaultTechnologies();
			FTGUConfig.cachedAllowResearchCopy = message.allowResearchCopy();
			FTGUConfig.cachedResearchGuideMode = message.researchGuideMode();
			// 服务端的档位可能和本地配置不一样（比如服务端禁用了指南），当场就对一次
			ClientHooks.applyResearchGuideMode.run();

			RegistryAccess ra;
			MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
			if (server != null)
				ra = server.registryAccess();
			else if (Minecraft.getInstance().getConnection() != null)
				ra = Minecraft.getInstance().getConnection().registryAccess();
			else
				ra = RegistryAccess.EMPTY;
			TechnologyManager.INSTANCE.setRegistryAccess(ra);

			TechnologyManager.INSTANCE.clear();

			TechnologyManager.INSTANCE.cache = message.json();
			TechnologyManager.INSTANCE.load();

			ClientHooks.initResearchBookGui.run();
			ClientHooks.clearToasts.run();

			PacketDispatcher.sendToServer(new RequestMessage());
		});
	}

	public static void handleHintMessage(HintMessage message, ClientPlayNetworking.Context ctx) {
		ctx.client().execute(() -> {
			Player player = ctx.player();
			if (player.containerMenu instanceof ContainerResearchTable table
					&& table.invInput.puzzle != null)
				table.invInput.puzzle.setHints(message.hints());
		});
	}

}
