package com.fuxingcheng.fromthegroundup;

import com.fuxingcheng.fromthegroundup.block.BlockIdeaTable;
import com.fuxingcheng.fromthegroundup.block.BlockResearchTable;
import com.fuxingcheng.fromthegroundup.criterion.TriggerInspect;
import com.fuxingcheng.fromthegroundup.criterion.TriggerItemInventory;
import com.fuxingcheng.fromthegroundup.criterion.TriggerRecipeLocked;
import com.fuxingcheng.fromthegroundup.criterion.TriggerTechnology;
import com.fuxingcheng.fromthegroundup.item.ItemMagnifyingGlass;
import com.fuxingcheng.fromthegroundup.item.ItemParchmentEmpty;
import com.fuxingcheng.fromthegroundup.item.ItemParchmentIdea;
import com.fuxingcheng.fromthegroundup.item.ItemParchmentResearch;
import com.fuxingcheng.fromthegroundup.item.ItemResearchBook;
import com.fuxingcheng.fromthegroundup.tileentity.TileEntityIdeaTable;
import com.fuxingcheng.fromthegroundup.tileentity.TileEntityResearchTable;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

public final class Content {

	public static final String n_ideaTable = "idea_table";
	public static final String n_researchTable = "research_table";
	public static final String n_parchmentEmpty = "parchment_empty";
	public static final String n_parchmentIdea = "parchment_idea";
	public static final String n_parchmentResearch = "parchment_research";
	public static final String n_researchBook = "research_book";
	public static final String n_magnifyingGlass = "magnifying_glass";

	// Blocks
	public static Block b_ideaTable;
	public static Block b_researchTable;

	// Items
	public static Item i_parchmentEmpty;
	public static Item i_parchmentIdea;
	public static Item i_parchmentResearch;
	public static Item i_researchBook;
	public static Item i_magnifyingGlass;
	public static BlockItem i_ideaTable;
	public static BlockItem i_researchTable;

	// Block Entities
	public static BlockEntityType<TileEntityIdeaTable> te_ideaTable;
	public static BlockEntityType<TileEntityResearchTable> te_researchTable;

	// Menus
	public static MenuType<com.fuxingcheng.fromthegroundup.inventory.ContainerIdeaTable> m_ideaTable;
	public static MenuType<com.fuxingcheng.fromthegroundup.inventory.ContainerResearchTable> m_researchTable;

	// Creative Tab
	public static CreativeModeTab FTGU_TAB;

	// Triggers
	public static TriggerTechnology c_technologyUnlocked;
	public static TriggerTechnology c_technologyResearched;
	public static TriggerRecipeLocked c_itemLocked;
	public static TriggerInspect c_inspect;
	public static TriggerItemInventory c_itemInventory;

	public static void registerAll() {
		// Register blocks
		b_ideaTable = registerBlock(n_ideaTable, new BlockIdeaTable());
		b_researchTable = registerBlock(n_researchTable, new BlockResearchTable());

		// Register items
		i_parchmentEmpty = registerItem(n_parchmentEmpty, new ItemParchmentEmpty(new Item.Properties()));
		i_parchmentIdea = registerItem(n_parchmentIdea, new ItemParchmentIdea(new Item.Properties()));
		i_parchmentResearch = registerItem(n_parchmentResearch, new ItemParchmentResearch(new Item.Properties()));
		i_researchBook = registerItem(n_researchBook, new ItemResearchBook(new Item.Properties()));
		i_magnifyingGlass = registerItem(n_magnifyingGlass, new ItemMagnifyingGlass(new Item.Properties()));

		// Register block items
		i_ideaTable = registerItem(n_ideaTable, new BlockItem(b_ideaTable, new Item.Properties()));
		i_researchTable = registerItem(n_researchTable, new BlockItem(b_researchTable, new Item.Properties()));

		// Register block entities
		te_ideaTable = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, n_ideaTable),
				BlockEntityType.Builder.of(TileEntityIdeaTable::new, b_ideaTable).build(null));
		te_researchTable = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, n_researchTable),
				BlockEntityType.Builder.of(TileEntityResearchTable::new, b_researchTable).build(null));

		// Register menus using ExtendedScreenHandlerType for Fabric
		m_ideaTable = Registry.register(BuiltInRegistries.MENU,
				ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, n_ideaTable),
				new ExtendedScreenHandlerType<>((syncId, playerInv, pos) -> {
					TileEntityIdeaTable tile = (TileEntityIdeaTable) playerInv.player.level().getBlockEntity(pos);
					return new com.fuxingcheng.fromthegroundup.inventory.ContainerIdeaTable(syncId, tile, playerInv);
				}, net.minecraft.core.BlockPos.STREAM_CODEC));
		m_researchTable = Registry.register(BuiltInRegistries.MENU,
				ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, n_researchTable),
				new ExtendedScreenHandlerType<>((syncId, playerInv, pos) -> {
					TileEntityResearchTable tile = (TileEntityResearchTable) playerInv.player.level().getBlockEntity(pos);
					return new com.fuxingcheng.fromthegroundup.inventory.ContainerResearchTable(syncId, tile, playerInv);
				}, net.minecraft.core.BlockPos.STREAM_CODEC));

		// Register creative tab
		FTGU_TAB = FabricItemGroup.builder()
				.title(Component.translatable("itemGroup.ftgumod"))
				.icon(() -> new ItemStack(i_researchBook))
				.displayItems((params, output) -> {
					output.accept(i_parchmentEmpty);
					output.accept(i_parchmentIdea);
					output.accept(i_parchmentResearch);
					output.accept(i_researchBook);
					output.accept(i_magnifyingGlass);
					output.accept(i_ideaTable);
					output.accept(i_researchTable);
				})
				.build();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
				ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "ftgumod"), FTGU_TAB);

		// Register triggers
		c_technologyUnlocked = registerTrigger("technology_unlocked", new TriggerTechnology("technology_unlocked"));
		c_technologyResearched = registerTrigger("technology_researched", new TriggerTechnology("technology_researched"));
		c_itemLocked = registerTrigger("recipe_locked", new TriggerRecipeLocked("recipe_locked"));
		c_inspect = registerTrigger("block_inspected", new TriggerInspect("block_inspected"));
		c_itemInventory = registerTrigger("item_inventory", new TriggerItemInventory("item_inventory"));
	}

	private static Block registerBlock(String name, Block block) {
		return Registry.register(BuiltInRegistries.BLOCK,
				ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, name), block);
	}

	private static <T extends Item> T registerItem(String name, T item) {
		return Registry.register(BuiltInRegistries.ITEM,
				ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, name), item);
	}

	private static <T extends CriterionTrigger<?>> T registerTrigger(String name, T trigger) {
		// Register trigger type directly to the registry
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, name);
		// In 1.21.1, we need to use the internal registry
		// CriteriaTriggers.register() should handle this
		net.minecraft.advancements.CriteriaTriggers.register(id.toString(), trigger);
		FromTheGroundUp.LOGGER.info("Registered trigger: {}", id);
		return trigger;
	}
}
