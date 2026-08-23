package com.Fuxingcheng.ftgumod.packet.client;

import java.util.HashMap;
import java.util.Map;

import com.Fuxingcheng.ftgumod.ClientHooks;
import com.Fuxingcheng.ftgumod.FTGUConfig;
import com.Fuxingcheng.ftgumod.packet.PacketDispatcher;
import com.Fuxingcheng.ftgumod.packet.server.RequestMessage;
import com.Fuxingcheng.ftgumod.technology.TechnologyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.apache.commons.lang3.tuple.Pair;

public class TechnologyInfoMessage {

	private final boolean allowResearchCopy;
	private final boolean loadDefaultTechnologies;
	private final FTGUConfig.HideJeiItems jeiHide;
	private final Map<String, Pair<String, Map<ResourceLocation, String>>> json;

	public TechnologyInfoMessage(boolean allowResearchCopy, boolean loadDefaultTechnologies,
			FTGUConfig.HideJeiItems jeiHide, Map<String, Pair<String, Map<ResourceLocation, String>>> json) {
		this.allowResearchCopy = allowResearchCopy;
		this.loadDefaultTechnologies = loadDefaultTechnologies;
		this.jeiHide = jeiHide;
		this.json = json;
	}

	public TechnologyInfoMessage(Map<String, Pair<String, Map<ResourceLocation, String>>> json) {
		this(FTGUConfig.cachedAllowResearchCopy, FTGUConfig.cachedLoadDefaultTechnologies, FTGUConfig.cachedJeiHide, json);
	}

	public static void encode(TechnologyInfoMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.allowResearchCopy);
		buf.writeBoolean(msg.loadDefaultTechnologies);
		buf.writeByte(msg.jeiHide.ordinal());

		buf.writeVarInt(msg.json.size());
		for (var entry : msg.json.entrySet()) {
			buf.writeUtf(entry.getKey());
			buf.writeUtf(entry.getValue().getLeft());
			buf.writeVarInt(entry.getValue().getRight().size());
			for (var techEntry : entry.getValue().getRight().entrySet()) {
				buf.writeUtf(techEntry.getKey().getPath());
				buf.writeUtf(techEntry.getValue());
			}
		}
	}

	public static TechnologyInfoMessage decode(FriendlyByteBuf buf) {
		boolean allowRC = buf.readBoolean();
		boolean loadDT = buf.readBoolean();
		FTGUConfig.HideJeiItems jeiH = FTGUConfig.HideJeiItems.values()[buf.readByte()];

		Map<String, Pair<String, Map<ResourceLocation, String>>> json = new HashMap<>();
		int size = buf.readVarInt();
		for (int i = 0; i < size; i++) {
			String domain = buf.readUtf();
			String context = buf.readUtf();
			int mapSize = buf.readVarInt();
			Map<ResourceLocation, String> map = new HashMap<>();
			for (int j = 0; j < mapSize; j++)
				map.put(ResourceLocation.fromNamespaceAndPath(domain, buf.readUtf()), buf.readUtf());
			json.put(domain, Pair.of(context, map));
		}

		return new TechnologyInfoMessage(allowRC, loadDT, jeiH, json);
	}

	public static void handle(TechnologyInfoMessage message, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			FTGUConfig.cachedLoadDefaultTechnologies = message.loadDefaultTechnologies;
			FTGUConfig.cachedAllowResearchCopy = message.allowResearchCopy;
			FTGUConfig.cachedJeiHide = message.jeiHide;

			net.minecraft.core.RegistryAccess ra;
			MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
			if (server != null)
				ra = server.registryAccess();
			else if (Minecraft.getInstance().getConnection() != null)
				ra = Minecraft.getInstance().getConnection().registryAccess();
			else
				ra = net.minecraft.core.RegistryAccess.EMPTY;
			TechnologyManager.INSTANCE.setRegistryAccess(ra);

			TechnologyManager.INSTANCE.clear();

			TechnologyManager.INSTANCE.cache = message.json;
			TechnologyManager.INSTANCE.load();

			ClientHooks.initResearchBookGui.run();
			ClientHooks.clearToasts.run();

			PacketDispatcher.sendToServer(new RequestMessage());
		});
		ctx.setPacketHandled(true);
	}
}
