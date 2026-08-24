package com.fuxingcheng.fromthegroundup.packet;

import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.packet.client.HintMessage;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyInfoMessage;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyMessage;
import com.fuxingcheng.fromthegroundup.packet.server.CopyTechMessage;
import com.fuxingcheng.fromthegroundup.packet.server.RequestHintsMessage;
import com.fuxingcheng.fromthegroundup.packet.server.RequestMessage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class PacketDispatcher {

	public static void registerPackets() {
		// Register client-to-server payload types
		PayloadTypeRegistry.playC2S().register(RequestMessage.TYPE, RequestMessage.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(CopyTechMessage.TYPE, CopyTechMessage.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(RequestHintsMessage.TYPE, RequestHintsMessage.STREAM_CODEC);

		// Register server-to-client payload types
		PayloadTypeRegistry.playS2C().register(TechnologyMessage.TYPE, TechnologyMessage.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(TechnologyInfoMessage.TYPE, TechnologyInfoMessage.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(HintMessage.TYPE, HintMessage.STREAM_CODEC);

		// Register server-side receivers for C2S packets
		ServerPlayNetworking.registerGlobalReceiver(RequestMessage.TYPE, (payload, context) -> {
			context.server().execute(() -> RequestMessage.handle(payload, context.player()));
		});
		ServerPlayNetworking.registerGlobalReceiver(CopyTechMessage.TYPE, (payload, context) -> {
			context.server().execute(() -> CopyTechMessage.handle(payload, context.player()));
		});
		ServerPlayNetworking.registerGlobalReceiver(RequestHintsMessage.TYPE, (payload, context) -> {
			context.server().execute(() -> RequestHintsMessage.handle(payload, context.player()));
		});
	}

	@Environment(EnvType.CLIENT)
	public static void registerClientReceivers() {
		// Register client-side receivers for S2C packets
		ClientPlayNetworking.registerGlobalReceiver(TechnologyMessage.TYPE, (payload, context) -> {
			TechnologyMessage.handle(payload, context);
		});
		ClientPlayNetworking.registerGlobalReceiver(TechnologyInfoMessage.TYPE, (payload, context) -> {
			TechnologyInfoMessage.handle(payload, context);
		});
		ClientPlayNetworking.registerGlobalReceiver(HintMessage.TYPE, (payload, context) -> {
			HintMessage.handle(payload, context);
		});
	}

	public static void sendTo(CustomPacketPayload message, ServerPlayer player) {
		ServerPlayNetworking.send(player, message);
	}

	public static void sendToAll(CustomPacketPayload message) {
		net.fabricmc.fabric.api.networking.v1.PlayerLookup.all(
				com.fuxingcheng.fromthegroundup.util.ServerHelper.getCurrentServer()
		).forEach(player -> ServerPlayNetworking.send(player, message));
	}

	public static void sendToServer(CustomPacketPayload message) {
		ClientPlayNetworking.send(message);
	}

}
