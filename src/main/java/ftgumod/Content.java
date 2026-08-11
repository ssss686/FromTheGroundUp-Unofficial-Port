package ftgumod;

import java.util.function.Supplier;

import ftgumod.block.BlockIdeaTable;
import ftgumod.block.BlockResearchTable;
import ftgumod.criterion.TriggerInspect;
import ftgumod.criterion.TriggerItemInventory;
import ftgumod.criterion.TriggerRecipeLocked;
import ftgumod.criterion.TriggerTechnology;
import ftgumod.item.ItemMagnifyingGlass;
import ftgumod.item.ItemParchmentEmpty;
import ftgumod.item.ItemParchmentIdea;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.item.ItemResearchBook;
import ftgumod.tileentity.TileEntityIdeaTable;
import ftgumod.tileentity.TileEntityResearchTable;
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
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

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

	public static final Supplier<MenuType<ftgumod.inventory.ContainerIdeaTable>> ideaTableMenu = MENU_TYPES.register(n_ideaTable,
			() -> IMenuTypeExtension.create((containerId, playerInv, extraData) -> {
				net.minecraft.core.BlockPos pos = extraData.readBlockPos();
				ftgumod.tileentity.TileEntityIdeaTable tile = (ftgumod.tileentity.TileEntityIdeaTable) playerInv.player.level().getBlockEntity(pos);
				return new ftgumod.inventory.ContainerIdeaTable(containerId, tile, playerInv);
			}));
	public static final Supplier<MenuType<ftgumod.inventory.ContainerResearchTable>> researchTableMenu = MENU_TYPES.register(n_researchTable,
			() -> IMenuTypeExtension.create((containerId, playerInv, extraData) -> {
				net.minecraft.core.BlockPos pos = extraData.readBlockPos();
				ftgumod.tileentity.TileEntityResearchTable tile = (ftgumod.tileentity.TileEntityResearchTable) playerInv.player.level().getBlockEntity(pos);
				return new ftgumod.inventory.ContainerResearchTable(containerId, tile, playerInv);
			}));

	public static final Supplier<MenuType<ftgumod.inventory.ContainerIdeaTable>> m_ideaTable = ideaTableMenu;
	public static final Supplier<MenuType<ftgumod.inventory.ContainerResearchTable>> m_researchTable = researchTableMenu;

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
