package ftgumod;

import java.util.List;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.platform.InputConstants;
import ftgumod.api.util.BlockSerializable;
import ftgumod.compat.jei.CompatJEI;
import ftgumod.event.PlayerLockEvent;
import ftgumod.item.ItemMagnifyingGlass;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyInfoMessage;
import ftgumod.client.FTGUClient;
import ftgumod.client.gui.GuiResearchBook;
import ftgumod.packet.server.RequestMessage;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.util.StackUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.lwjgl.glfw.GLFW;

public class EventHandler {

	private ItemStack stack = ItemStack.EMPTY;

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onItemTooltip(ItemTooltipEvent evt) {
		Item item = evt.getItemStack().getItem();
		if (item == Content.i_magnifyingGlass.get()) {
			List<BlockSerializable> blocks = ItemMagnifyingGlass.getInspected(evt.getItemStack());
			long window = Minecraft.getInstance().getWindow().getWindow();
			if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)
					|| InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)) {
				for (BlockSerializable block : blocks)
					evt.getToolTip().add(Component.literal(block.getLocalizedName())
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
				if (blocks.size() > 0)
					evt.getToolTip().add(Component.empty());
			} else if (blocks.size() > 0) {
				evt.getToolTip()
						.add(Component.translatable(Content.i_magnifyingGlass.get().getDescriptionId() + ".shift"));
				evt.getToolTip().add(Component.empty());
			}

			evt.getToolTip().add(Component.translatable("technology.decipher.tooltip")
					.withStyle(ChatFormatting.DARK_RED));
		} else if (item == Content.i_parchmentIdea.get()) {
			Technology tech = StackUtils.INSTANCE.getTechnology(evt.getItemStack());
			if (tech != null) {
				boolean hide = !tech.isResearched(evt.getEntity())
						&& !tech.canResearchIgnoreCustomUnlock(evt.getEntity());
				evt.getToolTip().add(Component.translatable("technology.idea",
						tech.getDisplayInfo().getTitle().getString()).withStyle(ChatFormatting.GOLD));
				evt.getToolTip().add(Component.literal(tech.getDisplayInfo().getDescription().getString())
						.withStyle(hide ? new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC,
								ChatFormatting.OBFUSCATED }
								: new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC }));
			}
		} else if (item == Content.i_parchmentResearch.get()) {
			Technology tech = StackUtils.INSTANCE.getTechnology(evt.getItemStack());
			if (tech != null) {
				boolean can = tech.isResearched(evt.getEntity())
						|| tech.canResearchIgnoreCustomUnlock(evt.getEntity());

				evt.getToolTip().add(Component.literal(tech.getDisplayInfo().getTitle().getString())
						.withStyle(ChatFormatting.GOLD));
				evt.getToolTip().add(Component.literal(tech.getDisplayInfo().getDescription().getString())
						.withStyle(can ? new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC }
								: new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC,
										ChatFormatting.OBFUSCATED }));

				if (can && !tech.isResearched(evt.getEntity())) {
					evt.getToolTip().add(Component.empty());
					evt.getToolTip().add(Component.translatable("item.parchment_research.complete")
							.withStyle(ChatFormatting.DARK_RED));
				}
			}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onKey(InputEvent.Key evt) {
		if (FTGUClient.KEY_RESEARCH_BOOK.isDown()) {
			Minecraft.getInstance().setScreen(new GuiResearchBook(Minecraft.getInstance().player));
			PacketDispatcher.sendToServer(new RequestMessage());
		}
	}

	@SubscribeEvent
	public void onItemCraft(PlayerEvent.ItemCraftedEvent evt) {
		if (evt.getCrafting().getItem() == Content.i_researchBook.get())
			for (int i = 0; i < evt.getInventory().getContainerSize(); i++) {
				ItemStack item = evt.getInventory().getItem(i);
				if (!item.isEmpty() && item.getItem() == Content.i_parchmentResearch.get())
					((ItemParchmentResearch) item.getItem()).research(item, evt.getEntity(), false);
			}
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
		if (!evt.getEntity().level().isClientSide()) {
			ServerPlayer player = (ServerPlayer) evt.getEntity();

			AbstractContainerMenu menu = player.containerMenu;
			menu.addSlotListener(new CraftingListener(player));

			CapabilityTechnology.ITechnology cap = player.getData(CapabilityTechnology.TECH_CAP.get());
			for (Technology tech : TechnologyManager.INSTANCE.getStart()) {
				if (!cap.isResearched(tech.getRegistryName().toString())) {
					cap.setResearched(tech.getRegistryName().toString());
					tech.addRecipes(player);
				}
			}
			if (cap.isNew()) {
				if (FTGUConfig.cachedGiveResearchBook) {
					player.getInventory().add(new ItemStack(Content.i_researchBook.get()));
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
		}
	}

	@SubscribeEvent
	public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent evt) {
		TechnologyManager.INSTANCE.unloadProgress(evt.getEntity());
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		if (!evt.getEntity().level().isClientSide()) {
			ServerPlayer player = (ServerPlayer) evt.getEntity();

			AbstractContainerMenu menu = player.containerMenu;
			menu.addSlotListener(new CraftingListener(player));
		}
	}

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerContainerEvent.Open evt) {
		if (!evt.getEntity().level().isClientSide()) {
			evt.getContainer().addSlotListener(new CraftingListener(evt.getEntity()));
		}
	}

	@SubscribeEvent
	public void onPlayerCloseContainer(PlayerContainerEvent.Close evt) {
		if (!evt.getEntity().level().isClientSide()) {
			evt.getEntity().containerMenu.addSlotListener(new CraftingListener(evt.getEntity()));
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onPlayerInGui(ScreenEvent.Render.Pre evt) {
		if (evt.getScreen() instanceof AbstractContainerScreen<?> screen) {
			AbstractContainerMenu menu = screen.getMenu();
			for (Slot s : menu.slots) {
				if (s.container instanceof ResultContainer resultContainer) {
					ItemStack slotStack = s.container.getItem(0);
					if (slotStack.isEmpty())
						this.stack = slotStack;
					else if (slotStack != this.stack) {
						PlayerLockEvent event = new PlayerLockEvent(
								Minecraft.getInstance().player, slotStack,
								resultContainer.getRecipeUsed());
						NeoForge.EVENT_BUS.post(event);

						if (!event.isCanceled())
							s.container.setItem(0, ItemStack.EMPTY);
						this.stack = s.container.getItem(0);
					}
					return;
				}
			}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide() && event.getEntity() == Minecraft.getInstance().player)
			PacketDispatcher.sendToServer(new RequestMessage());
	}

	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Post event) {
		for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
			var fakeMap = TechnologyManager.INSTANCE.getFakeAdvancements().get(player);
			if (fakeMap != null && !fakeMap.isEmpty()) {
				List<Pair<Technology, String>> toGrant = new ArrayList<>();
				for (var entry : fakeMap.entrySet()) {
					if (player.getAdvancements().getOrStartProgress(entry.getKey()).isDone())
						toGrant.add(entry.getValue());
				}
				for (var pair : toGrant)
					pair.getLeft().grantCriterion(player, pair.getRight());
			}
		}

		// Periodically check pending criteria for PlayerTrigger (location) and item_inventory
		if (event.getServer().getTickCount() % 20 == 0) {
			for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
				List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
				if (pending != null && !pending.isEmpty()) {
					List<TechnologyManager.PendingCriterion> matched = new ArrayList<>();
					for (TechnologyManager.PendingCriterion pc : pending) {
						if (pc.instance() instanceof PlayerTrigger.TriggerInstance pi) {
							var playerPred = pi.player();
							if (playerPred.isPresent()) {
								LootParams lootParams = new LootParams.Builder(player.serverLevel())
									.withParameter(LootContextParams.THIS_ENTITY, player)
									.withParameter(LootContextParams.ORIGIN, player.position())
									.withParameter(LootContextParams.BLOCK_STATE, player.getBlockStateOn())
									.withParameter(LootContextParams.TOOL, player.getMainHandItem())
									.create(LootContextParamSets.ADVANCEMENT_LOCATION);
								LootContext ctx = new LootContext.Builder(lootParams).create(Optional.empty());
								if (playerPred.get().matches(ctx))
									matched.add(pc);
							} else {
								matched.add(pc);
							}
						}
					}
					for (TechnologyManager.PendingCriterion pc : matched)
						pc.tech().grantCriterion(player, pc.criterionName());
				}
			}

			if (ftgumod.criterion.TriggerItemInventory.INSTANCE != null) {
				for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
					ftgumod.criterion.TriggerItemInventory.INSTANCE.trigger(player);
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntity().level().isClientSide())
			return;

		Entity killed = event.getEntity();
		Entity killer = event.getSource().getEntity();
		if (!(killer instanceof ServerPlayer player))
			return;

		List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
		if (pending == null || pending.isEmpty())
			return;

		List<TechnologyManager.PendingCriterion> matched = new ArrayList<>();
		for (TechnologyManager.PendingCriterion pc : pending) {
			if (pc.instance() instanceof KilledTrigger.TriggerInstance ki) {
				var entityPred = ki.entityPredicate();
				if (entityPred.isPresent()) {
					LootParams lootParams = new LootParams.Builder(player.serverLevel())
						.withParameter(LootContextParams.THIS_ENTITY, killed)
						.withParameter(LootContextParams.ORIGIN, player.position())
						.withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
						.create(LootContextParamSets.ADVANCEMENT_ENTITY);
					LootContext ctx = new LootContext.Builder(lootParams).create(Optional.empty());
					if (entityPred.get().matches(ctx))
						matched.add(pc);
				} else {
					matched.add(pc);
				}
			}
		}

		for (TechnologyManager.PendingCriterion pc : matched)
			pc.tech().grantCriterion(player, pc.criterionName());
	}

	@SubscribeEvent
	public void onMobEffect(MobEffectEvent.Added event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		if (player.level().isClientSide())
			return;

		List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
		if (pending == null || pending.isEmpty())
			return;

		List<TechnologyManager.PendingCriterion> matched = new ArrayList<>();
		for (TechnologyManager.PendingCriterion pc : pending) {
			if (pc.instance() instanceof EffectsChangedTrigger.TriggerInstance ei) {
				var effects = ei.effects();
				if (effects.isEmpty() || effects.get().matches(player))
					matched.add(pc);
			}
		}

		for (TechnologyManager.PendingCriterion pc : matched)
			pc.tech().grantCriterion(player, pc.criterionName());
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTick(ClientTickEvent.Pre event) {
		if (FTGU.JEI_LOADED)
			CompatJEI.refreshHiddenItems(true);
	}

}
