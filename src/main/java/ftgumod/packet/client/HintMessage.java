package ftgumod.packet.client;

import java.util.ArrayList;
import java.util.List;

import ftgumod.FTGU;
import ftgumod.inventory.ContainerResearchTable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HintMessage(List<Component> hints) implements CustomPacketPayload {

	public static final Type<HintMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "hint"));

	public static final StreamCodec<RegistryFriendlyByteBuf, HintMessage> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public HintMessage decode(RegistryFriendlyByteBuf buf) {
			List<Component> hints = new ArrayList<>();
			for (int i = 0; i < 9; i++)
				if (buf.readBoolean())
					hints.add(ComponentSerialization.STREAM_CODEC.decode(buf));
				else
					hints.add(null);
			return new HintMessage(hints);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, HintMessage msg) {
			for (int i = 0; i < 9; i++) {
				boolean b = msg.hints().size() > i && msg.hints().get(i) != null;
				buf.writeBoolean(b);
				if (b)
					ComponentSerialization.STREAM_CODEC.encode(buf, msg.hints().get(i));
			}
		}
	};

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(HintMessage message, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player.containerMenu instanceof ContainerResearchTable table
					&& table.invInput.puzzle != null)
				table.invInput.puzzle.setHints(message.hints());
		});
	}

}
