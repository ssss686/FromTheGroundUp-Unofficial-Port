package com.fuxingcheng.fromthegroundup.packet.server;

import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyMessage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record RequestMessage() implements CustomPacketPayload {

	public static final Type<RequestMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "request"));

	public static final StreamCodec<FriendlyByteBuf, RequestMessage> STREAM_CODEC = StreamCodec.unit(new RequestMessage());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(RequestMessage message, ServerPlayer player) {
		PacketDispatcher.sendTo(new TechnologyMessage(player, false), player);
	}

}
