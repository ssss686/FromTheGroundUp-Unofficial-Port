package ftgumod.packet;

import ftgumod.packet.client.HintMessage;
import ftgumod.packet.client.TechnologyInfoMessage;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.packet.server.CopyTechMessage;
import ftgumod.packet.server.RequestMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PacketDispatcher {

	public static void registerPackets(IEventBus modEventBus) {
		modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
			PayloadRegistrar registrar = event.registrar("1");

			registrar.playToClient(TechnologyMessage.TYPE, TechnologyMessage.STREAM_CODEC, TechnologyMessage::handle);
			registrar.playToClient(TechnologyInfoMessage.TYPE, TechnologyInfoMessage.STREAM_CODEC, TechnologyInfoMessage::handle);
			registrar.playToClient(HintMessage.TYPE, HintMessage.STREAM_CODEC, HintMessage::handle);

			registrar.playToServer(CopyTechMessage.TYPE, CopyTechMessage.STREAM_CODEC, CopyTechMessage::handle);
			registrar.playToServer(RequestMessage.TYPE, RequestMessage.STREAM_CODEC, RequestMessage::handle);
		});
	}

	public static void sendTo(TechnologyMessage message, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, message);
	}

	public static void sendTo(TechnologyInfoMessage message, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, message);
	}

	public static void sendTo(HintMessage message, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, message);
	}

	public static void sendToServer(CustomPacketPayload message) {
		PacketDistributor.sendToServer(message);
	}

	public static void sendToAll(TechnologyInfoMessage message) {
		PacketDistributor.sendToAllPlayers(message);
	}

}
