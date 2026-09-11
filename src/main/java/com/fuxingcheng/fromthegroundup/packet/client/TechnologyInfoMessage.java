package com.fuxingcheng.fromthegroundup.packet.client;

import java.util.HashMap;
import java.util.Map;

import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.FTGUConfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.apache.commons.lang3.tuple.Pair;

public record TechnologyInfoMessage(boolean allowResearchCopy, boolean loadDefaultTechnologies,
		FTGUConfig.ResearchGuideMode researchGuideMode, Map<String, Pair<String, Map<ResourceLocation, String>>> json)
		implements CustomPacketPayload {

	public TechnologyInfoMessage(Map<String, Pair<String, Map<ResourceLocation, String>>> json) {
		this(FTGUConfig.cachedAllowResearchCopy, FTGUConfig.cachedLoadDefaultTechnologies,
				FTGUConfig.cachedResearchGuideMode, json);
	}

	public static final Type<TechnologyInfoMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "technology_info"));

	public static final StreamCodec<FriendlyByteBuf, TechnologyInfoMessage> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public TechnologyInfoMessage decode(FriendlyByteBuf buf) {
			boolean allowRC = buf.readBoolean();
			boolean loadDT = buf.readBoolean();
			FTGUConfig.ResearchGuideMode guideMode = FTGUConfig.ResearchGuideMode.byOrdinal(buf.readByte());

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

			return new TechnologyInfoMessage(allowRC, loadDT, guideMode, json);
		}

		@Override
		public void encode(FriendlyByteBuf buf, TechnologyInfoMessage msg) {
			buf.writeBoolean(msg.allowResearchCopy());
			buf.writeBoolean(msg.loadDefaultTechnologies());
			buf.writeByte(msg.researchGuideMode().ordinal());

			buf.writeVarInt(msg.json().size());
			for (var entry : msg.json().entrySet()) {
				buf.writeUtf(entry.getKey());
				buf.writeUtf(entry.getValue().getLeft());
				buf.writeVarInt(entry.getValue().getRight().size());
				for (var techEntry : entry.getValue().getRight().entrySet()) {
					buf.writeUtf(techEntry.getKey().getPath());
					buf.writeUtf(techEntry.getValue());
				}
			}
		}
	};

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
