package ftgumod;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
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
import ftgumod.util.LootUtils;
import ftgumod.util.StackUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class EventHandler {

	private static final Logger LOGGER = LogManager.getLogger();

	private ItemStack stack = ItemStack.EMPTY;

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onItemTooltip(ItemTooltipEvent evt) {
		Item item = evt.getItemStack().getItem();
		if (item == Content.i_magnifyingGlass) {
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
		} else if (item == Content.i_parchmentIdea) {
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
		} else if (item == Content.i_parchmentResearch) {
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

			for (Technology tech : TechnologyManager.INSTANCE)
				if (tech.hasCustomUnlock() && tech.canResearchIgnoreCustomUnlock(player))
					tech.registerListeners(player);

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
			evt.getContainer().addSlotListener(new CraftingListener(evt.getEntity()));
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
	public void onLootTableLoad(LootTableLoadEvent event) {
		ResourceLocation name = event.getName();
		if (name.getPath().equals("chests/village_blacksmith"))
			injectLoot(event, ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "inject/blacksmith"));
		if (name.getPath().equals("chests/desert_pyramid"))
			injectLoot(event, ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "inject/pyramid"));
		if (name.getPath().equals("chests/stronghold_library"))
			injectLoot(event, ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "inject/library"));
	}

	private void injectLoot(LootTableLoadEvent event, ResourceLocation injectLoc) {
		try {
			String path = "/assets/" + injectLoc.getNamespace() + "/loot_tables/" + injectLoc.getPath() + ".json";
			Reader reader = new InputStreamReader(getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
			JsonElement json = GsonHelper.parse(reader);
			LootTable injectTable = LootTable.DIRECT_CODEC.parse(JsonOps.INSTANCE, json)
					.resultOrPartial(LOGGER::error).orElse(null);
			if (injectTable != null)
				event.setTable(LootUtils.merge(event.getTable(), injectTable));
		} catch (Exception e) {
			LOGGER.error("Failed to load inject loot table: {}", injectLoc, e);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide() && event.getEntity() == Minecraft.getInstance().player)
			PacketDispatcher.sendToServer(new RequestMessage());
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTick(ClientTickEvent.Pre event) {
		if (FTGU.JEI_LOADED)
			CompatJEI.refreshHiddenItems(true);
	}

}
