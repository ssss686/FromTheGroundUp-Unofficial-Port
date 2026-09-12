package ftgumod.client;

import ftgumod.ClientHooks;
import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.client.gui.GuiResearchBook;
import ftgumod.client.gui.toast.ToastTechnology;
import ftgumod.client.gui.GuiIdeaTable;
import ftgumod.client.gui.GuiResearchTable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = FTGU.MODID, value = Dist.CLIENT)
public final class FTGUClient {

	public static final KeyMapping KEY_RESEARCH_BOOK = new KeyMapping(
			"key.ftgumod.research_book",
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.ftgumod");

	/**
	 * 没装 JEI 时，「JEI 研究指南」这一项照样列在配置里，但按钮置灰、点不动，
	 * 悬停提示会说明原因 —— 让玩家知道本模组是适配 JEI 的，只是自己没装，
	 * 而不是"这个选项坏了"。
	 */
	public static final ConfigurationSectionScreen.Filter CONFIG_FILTER = (context, key, original) -> {
		if (!key.equals("researchGuideMode") || ModList.get().isLoaded("jei"))
			return original;

		AbstractWidget widget = original.getWidget(Minecraft.getInstance().options);
		widget.active = false;

		Component tooltip = Component.empty()
				.append(original.tooltip() != null ? original.tooltip() : Component.empty())
				.append(Component.literal("\n\n"))
				.append(Component.translatable("ftgumod.configuration.requiresjei")
						.withStyle(ChatFormatting.RED));
		widget.setTooltip(Tooltip.create(tooltip));
		return new ConfigurationSectionScreen.Element(original.name(), tooltip, widget, false);
	};

	@SubscribeEvent
	static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(Content.m_ideaTable.get(), GuiIdeaTable::new);
		event.register(Content.m_researchTable.get(), GuiResearchTable::new);
	}

	@SubscribeEvent
	static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(KEY_RESEARCH_BOOK);
	}


	static {
		ClientHooks.openResearchBook = p -> Minecraft.getInstance().setScreen(new GuiResearchBook(p));
		ClientHooks.displayToast = t -> Minecraft.getInstance().getToasts().addToast(new ToastTechnology(t));
		ClientHooks.clearToasts = () -> Minecraft.getInstance().getToasts().clear();
		// 局域网主机（单人、开了局域网）不算：那种时候读的就是服务端自己那份文件
		ClientHooks.isConnectedToRemoteServer = () -> {
			Minecraft minecraft = Minecraft.getInstance();
			return minecraft.getConnection() != null && !minecraft.hasSingleplayerServer();
		};
	}

}
