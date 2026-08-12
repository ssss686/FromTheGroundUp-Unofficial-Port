package ftgumod.packet.server;

import ftgumod.FTGU;
import ftgumod.inventory.ContainerResearchTable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestHintsMessage() implements CustomPacketPayload {

	public static final Type<RequestHintsMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "request_hints"));

	public static final StreamCodec<FriendlyByteBuf, RequestHintsMessage> STREAM_CODEC = StreamCodec.unit(new RequestHintsMessage());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(RequestHintsMessage message, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player().containerMenu instanceof ContainerResearchTable table && table.invInput.puzzle != null)
				table.invInput.puzzle.onInventoryChange(table);
		});
	}

}
