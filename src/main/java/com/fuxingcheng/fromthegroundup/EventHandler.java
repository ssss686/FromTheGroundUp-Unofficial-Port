package com.fuxingcheng.fromthegroundup;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import com.mojang.blaze3d.platform.InputConstants;
import com.fuxingcheng.fromthegroundup.api.util.BlockSerializable;
import com.fuxingcheng.fromthegroundup.compat.jei.CompatJEI;
import com.fuxingcheng.fromthegroundup.event.PlayerLockEvent;
import com.fuxingcheng.fromthegroundup.item.ItemMagnifyingGlass;
import com.fuxingcheng.fromthegroundup.item.ItemParchmentResearch;
import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyInfoMessage;
import com.fuxingcheng.fromthegroundup.client.FTGUClient;
import com.fuxingcheng.fromthegroundup.client.gui.GuiResearchBook;
import com.fuxingcheng.fromthegroundup.packet.server.RequestMessage;
import com.fuxingcheng.fromthegroundup.technology.CapabilityTechnology;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import com.fuxingcheng.fromthegroundup.util.StackUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

public class EventHandler {

	private static ItemStack stack = ItemStack.EMPTY;

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

		// Player join
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.getPlayer();
			server.execute(() -> {
				AbstractContainerMenu menu = player.containerMenu;
				menu.addSlotListener(new CraftingListener(player));

				CapabilityTechnology.ITechnology cap = player.getAttachedOrCreate(CapabilityTechnology.TECH_CAP);
				for (Technology tech : TechnologyManager.INSTANCE.getStart()) {
					if (!cap.isResearched(tech.getRegistryName().toString())) {
						cap.setResearched(tech.getRegistryName().toString());
						tech.addRecipes(player);
					}
				}
				if (cap.isNew()) {
					if (FTGUConfig.cachedGiveResearchBook) {
						player.getInventory().add(new ItemStack(Content.i_researchBook));
					}
					cap.setOld();
				}

				for (Technology tech : TechnologyManager.INSTANCE) {
					boolean hasCU = tech.hasCustomUnlock();
					boolean canRI = tech.canResearchIgnoreCustomUnlock(player);
					if (hasCU && canRI)
						tech.registerListeners(player);
				}

				PacketDispatcher.sendTo(new TechnologyInfoMessage(TechnologyManager.INSTANCE.cache), player);
			});
		});

		// Player leave
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			TechnologyManager.INSTANCE.unloadProgress(handler.getPlayer());
		});

		// Player clone (death)
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (!alive) {
				AbstractContainerMenu menu = newPlayer.containerMenu;
				menu.addSlotListener(new CraftingListener(newPlayer));
			}
		});

		// Server tick
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				var fakeMap = TechnologyManager.INSTANCE.getFakeAdvancements().get(player);
				if (fakeMap != null && !fakeMap.isEmpty()) {
					List<org.apache.commons.lang3.tuple.Pair<Technology, String>> toGrant = new ArrayList<>();
					for (var entry : fakeMap.entrySet()) {
						if (player.getAdvancements().getOrStartProgress(entry.getKey()).isDone())
							toGrant.add(entry.getValue());
					}
					for (var pair : toGrant)
						pair.getLeft().grantCriterion(player, pair.getRight());
				}
			}

			// Periodically check pending criteria
			if (server.getTickCount() % 20 == 0) {
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
					if (pending != null && !pending.isEmpty()) {
						List<TechnologyManager.PendingCriterion> matched = new ArrayList<>();
						for (TechnologyManager.PendingCriterion pc : pending) {
							// Simplified check - in NeoForge this checks PlayerTrigger.TriggerInstance
							// For now, just grant all pending location-based criteria
							matched.add(pc);
						}
						for (TechnologyManager.PendingCriterion pc : matched)
							pc.tech().grantCriterion(player, pc.criterionName());
					}
				}

				if (com.fuxingcheng.fromthegroundup.criterion.TriggerItemInventory.INSTANCE != null) {
					for (ServerPlayer player : server.getPlayerList().getPlayers()) {
						com.fuxingcheng.fromthegroundup.criterion.TriggerItemInventory.INSTANCE.trigger(player);
					}
				}
			}
		});

		// Entity join level (client side)
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			registerClientEvents();
		}
	}

	@Environment(EnvType.CLIENT)
	private static void registerClientEvents() {
		// Key input
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && FTGUClient.KEY_RESEARCH_BOOK.isDown()) {
				client.setScreen(new GuiResearchBook(client.player));
				PacketDispatcher.sendToServer(new RequestMessage());
			}

			// JEI compat
			if (FromTheGroundUp.JEI_LOADED)
				CompatJEI.refreshHiddenItems(true);
		});

		// Entity join level (client side)
		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity == Minecraft.getInstance().player) {
				PacketDispatcher.sendToServer(new RequestMessage());
			}
		});
	}

}
