package com.Fuxingcheng.ftgumod.packet.client;

import com.Fuxingcheng.ftgumod.ClientHooks;
import com.Fuxingcheng.ftgumod.FTGUConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * 运行期改了配置文件后，把新的研究指南档位单独推给客户端。
 *
 * 为什么不复用 TechnologyInfoMessage：那个还背着整份科技数据，客户端收到会重建整个
 * 科技管理器、清空吐司、重算研究之书，只为改一个显示档位太重了。
 */
public class ResearchGuideModeMessage {

	private final FTGUConfig.ResearchGuideMode mode;

	public ResearchGuideModeMessage(FTGUConfig.ResearchGuideMode mode) {
		this.mode = mode;
	}

	public static void encode(ResearchGuideModeMessage msg, FriendlyByteBuf buf) {
		buf.writeByte(msg.mode.ordinal());
	}

	public static ResearchGuideModeMessage decode(FriendlyByteBuf buf) {
		return new ResearchGuideModeMessage(FTGUConfig.ResearchGuideMode.byOrdinal(buf.readByte()));
	}

	public static void handle(ResearchGuideModeMessage message, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			FTGUConfig.cachedResearchGuideMode = message.mode;
			ClientHooks.applyResearchGuideMode.run();
		});
		ctx.setPacketHandled(true);
	}

}
