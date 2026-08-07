package ftgumod.packet.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ftgumod.FTGU;
import ftgumod.FTGUConfig;
import ftgumod.client.gui.GuiResearchBook;
import ftgumod.packet.server.RequestMessage;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.tuple.Pair;

public record TechnologyInfoMessage(boolean allowResearchCopy, boolean loadDefaultTechnologies,
		FTGUConfig.HideJeiItems jeiHide, Map<String, Pair<String, Map<ResourceLocation, String>>> json)
		implements CustomPacketPayload {

	public TechnologyInfoMessage(Map<String, Pair<String, Map<ResourceLocation, String>>> json) {
		this(FTGUConfig.cachedAllowResearchCopy, FTGUConfig.cachedLoadDefaultTechnologies, FTGUConfig.cachedJeiHide, json);
	}

	public static final Type<TechnologyInfoMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "technology_info"));

	public static final StreamCodec<FriendlyByteBuf, TechnologyInfoMessage> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public TechnologyInfoMessage decode(FriendlyByteBuf buf) {
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

		@Override
		public void encode(FriendlyByteBuf buf, TechnologyInfoMessage msg) {
			buf.writeBoolean(msg.allowResearchCopy());
			buf.writeBoolean(msg.loadDefaultTechnologies());
			buf.writeByte(msg.jeiHide().ordinal());

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

	public static void handle(TechnologyInfoMessage message, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player().level().isClientSide) {
				FTGUConfig.cachedLoadDefaultTechnologies = message.loadDefaultTechnologies();
				FTGUConfig.cachedAllowResearchCopy = message.allowResearchCopy();
				FTGUConfig.cachedJeiHide = message.jeiHide();

				TechnologyManager.INSTANCE.clear();

				TechnologyManager.INSTANCE.cache = message.json();
				TechnologyManager.INSTANCE.load();

				Supplier<Stream<Technology>> stream = TechnologyManager.INSTANCE.getRoots()::stream;
				GuiResearchBook.zoom = stream.get().collect(Collectors.toMap(Technology::getRegistryName, tech -> 1.0F));
				GuiResearchBook.xScrollO = stream.get().collect(Collectors.toMap(Technology::getRegistryName, tech -> -82.0));
				GuiResearchBook.yScrollO = stream.get().collect(Collectors.toMap(Technology::getRegistryName, tech -> -82.0));

				Minecraft.getInstance().getToasts().clear();
				PacketDistributor.sendToServer(new RequestMessage());
			}
		});
	}

}
