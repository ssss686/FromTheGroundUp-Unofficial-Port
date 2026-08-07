package ftgumod.packet.server;

import ftgumod.FTGU;
import ftgumod.packet.client.TechnologyMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestMessage() implements CustomPacketPayload {

	public static final Type<RequestMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "request"));

	public static final StreamCodec<FriendlyByteBuf, RequestMessage> STREAM_CODEC = StreamCodec.unit(new RequestMessage());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(RequestMessage message, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer sp)
				PacketDistributor.sendToPlayer(sp, new TechnologyMessage(sp, false));
		});
	}

}
