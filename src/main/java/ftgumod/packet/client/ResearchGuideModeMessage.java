package ftgumod.packet.client;

import ftgumod.ClientHooks;
import ftgumod.FTGU;
import ftgumod.FTGUConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 运行期改了配置文件后，把新的研究指南档位单独推给客户端。
 *
 * 为什么不复用 TechnologyInfoMessage：那个还背着整份科技数据，客户端收到会重建整个
 * 科技管理器、清空吐司、重算研究之书，只为改一个显示档位太重了。
 */
public record ResearchGuideModeMessage(FTGUConfig.ResearchGuideMode mode) implements CustomPacketPayload {

	public static final Type<ResearchGuideModeMessage> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "research_guide_mode"));

	public static final StreamCodec<FriendlyByteBuf, ResearchGuideModeMessage> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ResearchGuideModeMessage decode(FriendlyByteBuf buf) {
			return new ResearchGuideModeMessage(FTGUConfig.ResearchGuideMode.byOrdinal(buf.readByte()));
		}

		@Override
		public void encode(FriendlyByteBuf buf, ResearchGuideModeMessage msg) {
			buf.writeByte(msg.mode().ordinal());
		}
	};

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(ResearchGuideModeMessage message, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			FTGUConfig.cachedResearchGuideMode = message.mode();
			ClientHooks.applyResearchGuideMode.run();
		});
	}

}
