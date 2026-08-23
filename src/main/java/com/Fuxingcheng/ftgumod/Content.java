package com.Fuxingcheng.ftgumod;

import java.util.function.Supplier;

import com.Fuxingcheng.ftgumod.block.BlockIdeaTable;
import com.Fuxingcheng.ftgumod.block.BlockResearchTable;
import com.Fuxingcheng.ftgumod.criterion.TriggerInspect;
import com.Fuxingcheng.ftgumod.criterion.TriggerItemInventory;
import com.Fuxingcheng.ftgumod.criterion.TriggerRecipeLocked;
import com.Fuxingcheng.ftgumod.criterion.TriggerTechnology;
import com.Fuxingcheng.ftgumod.item.ItemMagnifyingGlass;
import com.Fuxingcheng.ftgumod.item.ItemParchmentEmpty;
import com.Fuxingcheng.ftgumod.item.ItemParchmentIdea;
import com.Fuxingcheng.ftgumod.item.ItemParchmentResearch;
import com.Fuxingcheng.ftgumod.item.ItemResearchBook;
import com.Fuxingcheng.ftgumod.tileentity.TileEntityIdeaTable;
import com.Fuxingcheng.ftgumod.tileentity.TileEntityResearchTable;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType; // 新增
import net.minecraftforge.registries.DeferredRegister; // 修改导入

public final class Content {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, FTGU.MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, FTGU.MODID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FTGU.MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FTGU.MODID);
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, FTGU.MODID);

	public static final String n_ideaTable = "idea_table";
	public static final String n_researchTable = "research_table";
	public static final String n_parchmentEmpty = "parchment_empty";
	public static final String n_parchmentIdea = "parchment_idea";
	public static final String n_parchmentResearch = "parchment_research";
	public static final String n_researchBook = "research_book";
	public static final String n_magnifyingGlass = "magnifying_glass";

	public static final Supplier<Block> b_ideaTable = BLOCKS.register(n_ideaTable, BlockIdeaTable::new);
	public static final Supplier<Block> b_researchTable = BLOCKS.register(n_researchTable, BlockResearchTable::new);

	public static final Supplier<Item> i_parchmentEmpty = ITEMS.register(n_parchmentEmpty, () -> new ItemParchmentEmpty(new Item.Properties()));
	public static final Supplier<Item> i_parchmentIdea = ITEMS.register(n_parchmentIdea, () -> new ItemParchmentIdea(new Item.Properties()));
	public static final Supplier<Item> i_parchmentResearch = ITEMS.register(n_parchmentResearch, () -> new ItemParchmentResearch(new Item.Properties()));
	public static final Supplier<Item> i_researchBook = ITEMS.register(n_researchBook, () -> new ItemResearchBook(new Item.Properties()));
	public static final Supplier<Item> i_magnifyingGlass = ITEMS.register(n_magnifyingGlass, () -> new ItemMagnifyingGlass(new Item.Properties()));

	public static final Supplier<BlockItem> i_ideaTable = ITEMS.register(n_ideaTable, () -> new BlockItem(b_ideaTable.get(), new Item.Properties()));
	public static final Supplier<BlockItem> i_researchTable = ITEMS.register(n_researchTable, () -> new BlockItem(b_researchTable.get(), new Item.Properties()));

	public static final Supplier<BlockEntityType<TileEntityIdeaTable>> te_ideaTable = BLOCK_ENTITY_TYPES.register(n_ideaTable,
			() -> BlockEntityType.Builder.of(TileEntityIdeaTable::new, b_ideaTable.get()).build(null));
	public static final Supplier<BlockEntityType<TileEntityResearchTable>> te_researchTable = BLOCK_ENTITY_TYPES.register(n_researchTable,
			() -> BlockEntityType.Builder.of(TileEntityResearchTable::new, b_researchTable.get()).build(null));

	// 修改：使用 IForgeMenuType.create 替换 IMenuTypeExtension.create
	public static final Supplier<MenuType<com.Fuxingcheng.ftgumod.inventory.ContainerIdeaTable>> ideaTableMenu = MENU_TYPES.register(n_ideaTable,
			() -> IForgeMenuType.create((windowId, inv, data) -> {
				net.minecraft.core.BlockPos pos = data.readBlockPos();
				com.Fuxingcheng.ftgumod.tileentity.TileEntityIdeaTable tile = (com.Fuxingcheng.ftgumod.tileentity.TileEntityIdeaTable) inv.player.level().getBlockEntity(pos);
				return new com.Fuxingcheng.ftgumod.inventory.ContainerIdeaTable(windowId, tile, inv);
			}));
	public static final Supplier<MenuType<com.Fuxingcheng.ftgumod.inventory.ContainerResearchTable>> researchTableMenu = MENU_TYPES.register(n_researchTable,
			() -> IForgeMenuType.create((windowId, inv, data) -> {
				net.minecraft.core.BlockPos pos = data.readBlockPos();
				com.Fuxingcheng.ftgumod.tileentity.TileEntityResearchTable tile = (com.Fuxingcheng.ftgumod.tileentity.TileEntityResearchTable) inv.player.level().getBlockEntity(pos);
				return new com.Fuxingcheng.ftgumod.inventory.ContainerResearchTable(windowId, tile, inv);
			}));

	public static final Supplier<MenuType<com.Fuxingcheng.ftgumod.inventory.ContainerIdeaTable>> m_ideaTable = ideaTableMenu;
	public static final Supplier<MenuType<com.Fuxingcheng.ftgumod.inventory.ContainerResearchTable>> m_researchTable = researchTableMenu;

	public static final Supplier<CreativeModeTab> FTGU_TAB = CREATIVE_MODE_TABS.register("ftgumod",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.ftgumod"))
					.icon(() -> new ItemStack(i_researchBook.get()))
					.displayItems((params, output) -> {
						output.accept(i_parchmentEmpty.get());
						output.accept(i_parchmentIdea.get());
						output.accept(i_parchmentResearch.get());
						output.accept(i_researchBook.get());
						output.accept(i_magnifyingGlass.get());
						output.accept(i_ideaTable.get());
						output.accept(i_researchTable.get());
					})
					.build());

	public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES = DeferredRegister.create(Registries.TRIGGER_TYPE, FTGU.MODID);

	public static final Supplier<TriggerTechnology> c_technologyUnlocked = TRIGGER_TYPES.register("technology_unlocked", () -> new TriggerTechnology("technology_unlocked"));
	public static final Supplier<TriggerTechnology> c_technologyResearched = TRIGGER_TYPES.register("technology_researched", () -> new TriggerTechnology("technology_researched"));
	public static final Supplier<TriggerRecipeLocked> c_itemLocked = TRIGGER_TYPES.register("recipe_locked", () -> new TriggerRecipeLocked("recipe_locked"));
	public static final Supplier<TriggerInspect> c_inspect = TRIGGER_TYPES.register("block_inspected", () -> new TriggerInspect("block_inspected"));
	public static final Supplier<TriggerItemInventory> c_itemInventory = TRIGGER_TYPES.register("item_inventory", () -> new TriggerItemInventory("item_inventory"));

}