package com.Fuxingcheng.ftgumod.packet.server;

import com.Fuxingcheng.ftgumod.inventory.ContainerResearchTable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class RequestHintsMessage {

	public RequestHintsMessage() {
	}

	public static void encode(RequestHintsMessage msg, FriendlyByteBuf buf) {
	}

	public static RequestHintsMessage decode(FriendlyByteBuf buf) {
		return new RequestHintsMessage();
	}

	public static void handle(RequestHintsMessage message, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer sp = ctx.getSender();
			if (sp != null && sp.containerMenu instanceof ContainerResearchTable table
					&& table.invInput.puzzle != null)
				table.invInput.puzzle.onInventoryChange(table);
		});
		ctx.setPacketHandled(true);
	}
}
