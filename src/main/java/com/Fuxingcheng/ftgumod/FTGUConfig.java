package com.Fuxingcheng.ftgumod;

import com.Fuxingcheng.ftgumod.packet.PacketDispatcher;
import com.Fuxingcheng.ftgumod.packet.client.ResearchGuideModeMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = FTGU.MODID)
public final class FTGUConfig {

	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.BooleanValue allowResearchCopy;
	public static final ForgeConfigSpec.BooleanValue loadDefaultTechnologies;
	public static final ForgeConfigSpec.BooleanValue giveResearchBook;
	public static final ForgeConfigSpec.EnumValue<ResearchGuideMode> researchGuideMode;

	public static boolean cachedLoadDefaultTechnologies;
	public static boolean cachedGiveResearchBook;
	public static boolean cachedAllowResearchCopy;
	public static ResearchGuideMode cachedResearchGuideMode;

	/**
	 * JEI 研究指南显示档位
	 */
	public enum ResearchGuideMode {
		/** 前置科技链 + 研究方法（创作台原料、研究台谜题、附加解锁条件） */
		FULL,
		/** 只显示前置科技链 */
		CHAIN_ONLY,
		/** 不显示研究指南：JEI 里连栏位一起隐藏 */
		DISABLED;

		/**
		 * 按序数取值，越界退回默认档。
		 *
		 * 档位会随 TechnologyInfoMessage 从服务端发到客户端，两端模组版本不一致时
		 * 序数可能对不上，不能直接下标取值。
		 */
		public static ResearchGuideMode byOrdinal(int ordinal) {
			ResearchGuideMode[] values = values();
			return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FULL;
		}
	}

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		builder.comment("FTGU Mod Configuration").push("ftgumod");

		allowResearchCopy = builder
				.comment("If enabled, researches can be copied")
				.define("allowResearchCopy", true);

		loadDefaultTechnologies = builder
				.comment("If disabled, default technologies will not be loaded")
				.define("loadDefaultTechnologies", true);

		giveResearchBook = builder
				.comment("If enabled, every player will get a research book when they join a new world or server")
				.define("giveResearchBook", true);

		researchGuideMode = builder
				.comment("How much of the research guide is shown in JEI.",
						"FULL = prerequisite technologies, and how to research them (idea table ingredients, research table puzzle, other unlock conditions)",
						"CHAIN_ONLY = prerequisite technologies only",
						"DISABLED = no research guide in JEI at all")
				.defineEnum("researchGuideMode", ResearchGuideMode.FULL);

		builder.pop();

		SPEC = builder.build();
		cachedLoadDefaultTechnologies = true;
		cachedGiveResearchBook = true;
		cachedAllowResearchCopy = true;
		cachedResearchGuideMode = ResearchGuideMode.FULL;
	}

	@SubscribeEvent
	public static void onLoad(ModConfigEvent event) {
		if (!event.getConfig().getModId().equals(FTGU.MODID))
			return;

		// 连着别人的服务器时本地文件说了不算：这几个值进服时已经由服务端发过来了
		// （见 TechnologyInfoMessage）。不拦住的话，玩家只要在配置界面动任何一项再关闭
		// （关闭会保存 → Reloading），本地文件里的档位就会把服务端的设置顶掉。
		// 单人、局域网主机，以及还没连上服务器的时候照旧认本地文件。
		if (event instanceof ModConfigEvent.Reloading && ClientHooks.isConnectedToRemoteServer.getAsBoolean())
			return;

		cachedLoadDefaultTechnologies = loadDefaultTechnologies.get();
		cachedGiveResearchBook = giveResearchBook.get();
		cachedAllowResearchCopy = allowResearchCopy.get();

		ResearchGuideMode mode = researchGuideMode.get();
		boolean modeChanged = cachedResearchGuideMode != mode;
		cachedResearchGuideMode = mode;

		// 只有运行期改文件才走这里（初次加载是 ModConfigEvent.Loading）。
		// 指南档位是唯一能立刻生效的一个：本地 JEI 栏位马上跟着变，
		// 已连上的客户端靠这个包 —— 通用配置 Forge 不会自动同步给客户端。
		// 本方法可能在配置文件监听线程上跑，两件事都得自己回到对应的线程去。
		if (modeChanged && event instanceof ModConfigEvent.Reloading)
			applyResearchGuideMode();
	}

	/** 把新档位送到本地客户端的 JEI 和所有已连上的客户端 */
	private static void applyResearchGuideMode() {
		ClientHooks.applyResearchGuideMode.run();

		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null && server.isRunning())
			// 发在服务端主线程上：本方法可能在文件监听线程上跑，而发包得在主线程
			server.execute(() -> PacketDispatcher.sendToAll(
					new ResearchGuideModeMessage(cachedResearchGuideMode)));
	}

}
