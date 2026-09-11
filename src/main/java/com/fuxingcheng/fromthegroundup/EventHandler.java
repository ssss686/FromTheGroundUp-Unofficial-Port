package com.fuxingcheng.fromthegroundup;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import com.fuxingcheng.fromthegroundup.event.PlayerLockEvent;
import com.fuxingcheng.fromthegroundup.item.ItemParchmentResearch;
import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyInfoMessage;
import com.fuxingcheng.fromthegroundup.packet.server.RequestMessage;
import com.fuxingcheng.fromthegroundup.technology.CapabilityTechnology;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import com.fuxingcheng.fromthegroundup.util.RecipeHideHelper;
import com.fuxingcheng.fromthegroundup.util.StackUtils;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EventHandler {

	private static ItemStack stack = ItemStack.EMPTY;

	public static void register() {
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

				// Clean locked recipes from recipe book
				RecipeHideHelper.cleanRecipeBook(player);
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
					// Collect completed advancements first to avoid concurrent modification
					List<org.apache.commons.lang3.tuple.Pair<Technology, String>> toGrant = new ArrayList<>();
					for (var entry : new java.util.HashMap<>(fakeMap).entrySet()) {
						var progress = player.getAdvancements().getOrStartProgress(entry.getKey());
						if (progress.isDone()) {
							toGrant.add(entry.getValue());
						}
					}
					for (var pair : toGrant) {
						pair.getLeft().grantCriterion(player, pair.getRight());
					}
				}
			}

			// Note: Pending criteria are handled by vanilla trigger system
			// The criteria will be granted when their triggers fire naturally
			if (server.getTickCount() % 20 == 0) {

				if (com.fuxingcheng.fromthegroundup.criterion.TriggerItemInventory.INSTANCE != null) {
					for (ServerPlayer player : server.getPlayerList().getPlayers()) {
						com.fuxingcheng.fromthegroundup.criterion.TriggerItemInventory.INSTANCE.trigger(player);
					}
				}
			}
		});

	}

}
