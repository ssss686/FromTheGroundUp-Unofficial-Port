package com.Fuxingcheng.ftgumod.client;

import com.Fuxingcheng.ftgumod.ClientHooks;
import com.Fuxingcheng.ftgumod.Content;
import com.Fuxingcheng.ftgumod.FTGU;
import com.Fuxingcheng.ftgumod.client.gui.GuiResearchBook;
import com.Fuxingcheng.ftgumod.client.gui.toast.ToastTechnology;
import com.Fuxingcheng.ftgumod.technology.Technology;
import com.Fuxingcheng.ftgumod.technology.TechnologyManager;
import com.Fuxingcheng.ftgumod.client.gui.GuiIdeaTable;
import com.Fuxingcheng.ftgumod.client.gui.GuiResearchTable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = FTGU.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class FTGUClient {

	public static final KeyMapping KEY_RESEARCH_BOOK = new KeyMapping(
			"key.ftgumod.research_book",
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.ftgumod");

	@SubscribeEvent
	static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(Content.m_ideaTable.get(), GuiIdeaTable::new);
			MenuScreens.register(Content.m_researchTable.get(), GuiResearchTable::new);
		});
	}

	@SubscribeEvent
	static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(KEY_RESEARCH_BOOK);
	}

	static {
		ClientHooks.openResearchBook = p -> Minecraft.getInstance().setScreen(new GuiResearchBook(p));
		ClientHooks.displayToast = t -> Minecraft.getInstance().getToasts().addToast(new ToastTechnology(t));
		ClientHooks.clearToasts = () -> Minecraft.getInstance().getToasts().clear();
		ClientHooks.initResearchBookGui = () -> {
			java.util.function.Supplier<java.util.stream.Stream<Technology>> stream = TechnologyManager.INSTANCE.getRoots()::stream;
			GuiResearchBook.zoom = stream.get().collect(java.util.stream.Collectors.toMap(Technology::getRegistryName, tech -> 1.0F));
			GuiResearchBook.xScrollO = stream.get().collect(java.util.stream.Collectors.toMap(Technology::getRegistryName, tech -> -82.0));
			GuiResearchBook.yScrollO = stream.get().collect(java.util.stream.Collectors.toMap(Technology::getRegistryName, tech -> -82.0));
		};
	}
}