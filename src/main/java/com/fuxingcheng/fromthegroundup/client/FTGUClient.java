package com.fuxingcheng.fromthegroundup.client;

import com.fuxingcheng.fromthegroundup.ClientHooks;
import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.client.gui.GuiIdeaTable;
import com.fuxingcheng.fromthegroundup.client.gui.GuiResearchBook;
import com.fuxingcheng.fromthegroundup.client.gui.GuiResearchTable;
import com.fuxingcheng.fromthegroundup.client.gui.toast.ToastTechnology;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;

import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class FTGUClient implements ClientModInitializer {

	public static final KeyMapping KEY_RESEARCH_BOOK = new KeyMapping(
			"key.ftgumod.research_book",
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.ftgumod");

	@Override
	public void onInitializeClient() {
		// Register client-only event handlers (item tooltips, key input, entity join)
		ClientEventHandler.register();

		// Register client-side packet receivers
		PacketDispatcher.registerClientReceivers();

		// Register key bindings
		KeyBindingHelper.registerKeyBinding(KEY_RESEARCH_BOOK);

		// Register screen handlers
		MenuScreens.register(Content.m_ideaTable, GuiIdeaTable::new);
		MenuScreens.register(Content.m_researchTable, GuiResearchTable::new);

		// Setup client hooks
		ClientHooks.openResearchBook = p -> Minecraft.getInstance().setScreen(new GuiResearchBook(p));
		ClientHooks.displayToast = t -> Minecraft.getInstance().getToasts().addToast(new ToastTechnology(t));
		ClientHooks.clearToasts = () -> Minecraft.getInstance().getToasts().clear();

		FromTheGroundUp.LOGGER.info("FromTheGroundUp client initialized!");
	}

}
