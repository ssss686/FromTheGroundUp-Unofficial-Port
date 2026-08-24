package com.fuxingcheng.fromthegroundup.packet.server;

import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.inventory.ContainerResearchTable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record RequestHintsMessage() implements CustomPacketPayload {

	public static final Type<RequestHintsMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "request_hints"));

	public static final StreamCodec<FriendlyByteBuf, RequestHintsMessage> STREAM_CODEC = StreamCodec.unit(new RequestHintsMessage());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(RequestHintsMessage message, ServerPlayer player) {
		if (player.containerMenu instanceof ContainerResearchTable table && table.invInput.puzzle != null)
			table.invInput.puzzle.onInventoryChange(table);
	}

}
