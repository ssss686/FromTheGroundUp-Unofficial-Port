package com.Fuxingcheng.ftgumod.packet.client;

import java.util.ArrayList;
import java.util.List;

import com.Fuxingcheng.ftgumod.inventory.ContainerResearchTable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class HintMessage {

	private final List<Component> hints;

	public HintMessage(List<Component> hints) {
		this.hints = hints;
	}

	public static void encode(HintMessage msg, FriendlyByteBuf buf) {
		RegistryFriendlyByteBuf rBuf = (RegistryFriendlyByteBuf) buf;
		for (int i = 0; i < 9; i++) {
			boolean b = msg.hints.size() > i && msg.hints.get(i) != null;
			buf.writeBoolean(b);
			if (b)
				ComponentSerialization.STREAM_CODEC.encode(rBuf, msg.hints.get(i));
		}
	}

	public static HintMessage decode(FriendlyByteBuf buf) {
		RegistryFriendlyByteBuf rBuf = (RegistryFriendlyByteBuf) buf;
		List<Component> hints = new ArrayList<>();
		for (int i = 0; i < 9; i++)
			if (buf.readBoolean())
				hints.add(ComponentSerialization.STREAM_CODEC.decode(rBuf));
			else
				hints.add(null);
		return new HintMessage(hints);
	}

	public static void handle(HintMessage message, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.getSender();
			if (player == null)
				player = net.minecraft.client.Minecraft.getInstance().player;
			if (player != null && player.containerMenu instanceof ContainerResearchTable table
					&& table.invInput.puzzle != null)
				table.invInput.puzzle.setHints(message.hints);
		});
		ctx.setPacketHandled(true);
	}
}
