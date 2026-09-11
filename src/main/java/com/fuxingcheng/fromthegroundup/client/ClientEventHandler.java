package com.fuxingcheng.fromthegroundup.client;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.api.util.BlockSerializable;
import com.fuxingcheng.fromthegroundup.client.gui.GuiResearchBook;
import com.fuxingcheng.fromthegroundup.item.ItemMagnifyingGlass;
import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.packet.server.RequestMessage;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.util.StackUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import org.lwjgl.glfw.GLFW;

/**
 * Client-only event handlers.
 * These live in their own class so that {@link com.fuxingcheng.fromthegroundup.EventHandler}
 * stays free of client classes: a dedicated server cannot load {@code net.minecraft.client.*}
 * classes, and the class verifier loads them as soon as they appear in a method body.
 * Registered from {@link FTGUClient}.
 */
@Environment(EnvType.CLIENT)
public final class ClientEventHandler {

	public static void register() {
		// Item tooltip
		ItemTooltipCallback.EVENT.register((itemStack, context, flag, lines) -> {
			Item item = itemStack.getItem();
			if (item == Content.i_magnifyingGlass) {
				List<BlockSerializable> blocks = ItemMagnifyingGlass.getInspected(itemStack);
				long window = Minecraft.getInstance().getWindow().getWindow();
				if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)
						|| InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)) {
					for (BlockSerializable block : blocks)
						lines.add(Component.literal(block.getLocalizedName())
								.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
					if (blocks.size() > 0)
						lines.add(Component.empty());
				} else if (blocks.size() > 0) {
					lines.add(Component.translatable(Content.i_magnifyingGlass.getDescriptionId() + ".shift"));
					lines.add(Component.empty());
				}
				lines.add(Component.translatable("technology.decipher.tooltip")
						.withStyle(ChatFormatting.DARK_RED));
			} else if (item == Content.i_parchmentIdea) {
				Technology tech = StackUtils.INSTANCE.getTechnology(itemStack);
				if (tech != null) {
					boolean hide = !tech.isResearched(Minecraft.getInstance().player)
							&& !tech.canResearchIgnoreCustomUnlock(Minecraft.getInstance().player);
					lines.add(Component.translatable("technology.idea",
							tech.getDisplayInfo().getTitle().getString()).withStyle(ChatFormatting.GOLD));
					lines.add(Component.literal(tech.getDisplayInfo().getDescription().getString())
							.withStyle(hide ? new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC,
									ChatFormatting.OBFUSCATED }
									: new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC }));
				}
			} else if (item == Content.i_parchmentResearch) {
				Technology tech = StackUtils.INSTANCE.getTechnology(itemStack);
				if (tech != null) {
					boolean can = tech.isResearched(Minecraft.getInstance().player)
							|| tech.canResearchIgnoreCustomUnlock(Minecraft.getInstance().player);
					lines.add(Component.literal(tech.getDisplayInfo().getTitle().getString())
							.withStyle(ChatFormatting.GOLD));
					lines.add(Component.literal(tech.getDisplayInfo().getDescription().getString())
							.withStyle(can ? new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC }
									: new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC,
											ChatFormatting.OBFUSCATED }));
					if (can && !tech.isResearched(Minecraft.getInstance().player)) {
						lines.add(Component.empty());
						lines.add(Component.translatable("item.ftgumod.parchment_research.complete")
								.withStyle(ChatFormatting.DARK_RED));
					}
				}
			}
		});

		// Key input
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && FTGUClient.KEY_RESEARCH_BOOK.isDown()) {
				client.setScreen(new GuiResearchBook(client.player));
				PacketDispatcher.sendToServer(new RequestMessage());
			}
		});

		// Entity join level (client side)
		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity == Minecraft.getInstance().player) {
				PacketDispatcher.sendToServer(new RequestMessage());
			}
		});
	}

}
