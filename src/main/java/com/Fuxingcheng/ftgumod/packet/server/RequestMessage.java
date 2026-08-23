package com.Fuxingcheng.ftgumod.packet.server;

import com.Fuxingcheng.ftgumod.packet.PacketDispatcher;
import com.Fuxingcheng.ftgumod.packet.client.TechnologyMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class RequestMessage {

	public RequestMessage() {
	}

	public static void encode(RequestMessage msg, FriendlyByteBuf buf) {
	}

	public static RequestMessage decode(FriendlyByteBuf buf) {
		return new RequestMessage();
	}

	public static void handle(RequestMessage message, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer sp = ctx.getSender();
			if (sp != null)
				PacketDispatcher.sendTo(new TechnologyMessage(sp, false), sp);
		});
		ctx.setPacketHandled(true);
	}
}
