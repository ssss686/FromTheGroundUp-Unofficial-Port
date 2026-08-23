package com.Fuxingcheng.ftgumod.packet;

import com.Fuxingcheng.ftgumod.FTGU;
import com.Fuxingcheng.ftgumod.packet.client.HintMessage;
import com.Fuxingcheng.ftgumod.packet.client.TechnologyInfoMessage;
import com.Fuxingcheng.ftgumod.packet.client.TechnologyMessage;
import com.Fuxingcheng.ftgumod.packet.server.CopyTechMessage;
import com.Fuxingcheng.ftgumod.packet.server.RequestHintsMessage;
import com.Fuxingcheng.ftgumod.packet.server.RequestMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class PacketDispatcher {

	public static final SimpleChannel CHANNEL = ChannelBuilder
			.named(FTGU.MODID + ":main")
			.networkProtocolVersion(1)
			.simpleChannel();

	private static int packetId = 0;

	public static void registerPackets() {
		// 客户端接收
		CHANNEL.messageBuilder(TechnologyMessage.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(TechnologyMessage::encode)
				.decoder(TechnologyMessage::decode)
				.consumerMainThread(TechnologyMessage::handle)
				.add();

		CHANNEL.messageBuilder(TechnologyInfoMessage.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(TechnologyInfoMessage::encode)
				.decoder(TechnologyInfoMessage::decode)
				.consumerMainThread(TechnologyInfoMessage::handle)
				.add();

		CHANNEL.messageBuilder(HintMessage.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(HintMessage::encode)
				.decoder(HintMessage::decode)
				.consumerMainThread(HintMessage::handle)
				.add();

		// 服务器接收
		CHANNEL.messageBuilder(RequestMessage.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(RequestMessage::encode)
				.decoder(RequestMessage::decode)
				.consumerMainThread(RequestMessage::handle)
				.add();

		CHANNEL.messageBuilder(RequestHintsMessage.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(RequestHintsMessage::encode)
				.decoder(RequestHintsMessage::decode)
				.consumerMainThread(RequestHintsMessage::handle)
				.add();

		CHANNEL.messageBuilder(CopyTechMessage.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(CopyTechMessage::encode)
				.decoder(CopyTechMessage::decode)
				.consumerMainThread(CopyTechMessage::handle)
				.add();
	}

	public static void sendTo(TechnologyMessage message, ServerPlayer player) {
		CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
	}

	public static void sendTo(TechnologyInfoMessage message, ServerPlayer player) {
		CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
	}

	public static void sendTo(HintMessage message, ServerPlayer player) {
		CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
	}

	public static void sendToServer(Object message) {
		CHANNEL.send(message, PacketDistributor.SERVER.noArg());
	}

	public static void sendToAll(TechnologyInfoMessage message) {
		CHANNEL.send(message, PacketDistributor.ALL.noArg());
	}
}
