package com.fuxingcheng.fromthegroundup.packet.client;

import com.fuxingcheng.fromthegroundup.ClientHooks;
import com.fuxingcheng.fromthegroundup.FTGUConfig;
import com.fuxingcheng.fromthegroundup.FromTheGroundUp;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 运行期改了配置文件后，把新的研究指南档位单独推给客户端。
 *
 * 为什么不复用 TechnologyInfoMessage：那个还背着整份科技数据，客户端收到会重建整个
 * 科技管理器、清空吐司、重算研究之书，只为改一个显示档位太重了。
 */
public record ResearchGuideModeMessage(FTGUConfig.ResearchGuideMode mode) implements CustomPacketPayload {

	public static final Type<ResearchGuideModeMessage> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "research_guide_mode"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ResearchGuideModeMessage> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public ResearchGuideModeMessage decode(RegistryFriendlyByteBuf buf) {
			return new ResearchGuideModeMessage(FTGUConfig.ResearchGuideMode.byOrdinal(buf.readByte()));
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, ResearchGuideModeMessage msg) {
			buf.writeByte(msg.mode().ordinal());
		}
	};

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(ResearchGuideModeMessage message, ClientPlayNetworking.Context ctx) {
		FTGUConfig.cachedResearchGuideMode = message.mode();
		ClientHooks.applyResearchGuideMode.run();
	}

}
