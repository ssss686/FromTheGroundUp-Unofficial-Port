package ftgumod.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.Content;
import ftgumod.api.FTGUAPI;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.util.BlockSerializable;
import ftgumod.api.util.IStackUtils;
import ftgumod.api.util.JsonContextPublic;
import ftgumod.api.util.predicate.ItemCompound;
import ftgumod.api.util.predicate.ItemIngredient;
import ftgumod.api.util.predicate.ItemPredicate;
import ftgumod.item.ItemMagnifyingGlass;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;

import com.mojang.serialization.JsonOps;

public class StackUtils implements IStackUtils {

	public static final StackUtils INSTANCE = new StackUtils();

	private static final Map<ResourceLocation, ItemPredicate.Factory> REGISTRY = new HashMap<>();

	private static final Map<String, String> ORE_DICT_TO_TAG = new HashMap<>();

	private static final Map<String, String> ITEM_REMAP = new HashMap<>();
	private static final Map<String, Map<Integer, String>> DATA_REMAP = new HashMap<>();

	private static ItemStack[] TIPPED_ARROW_STACKS = null;


	static {
		FTGUAPI.stackUtils = INSTANCE;

		ORE_DICT_TO_TAG.put("plankWood", "minecraft:planks");
		ORE_DICT_TO_TAG.put("slabWood", "minecraft:wooden_slabs");
		ORE_DICT_TO_TAG.put("stairWood", "minecraft:wooden_stairs");
		ORE_DICT_TO_TAG.put("stickWood", "c:rods/wooden");
		ORE_DICT_TO_TAG.put("paneGlassColorless", "c:glass_panes/colorless");
		ORE_DICT_TO_TAG.put("oreIron", "c:ores/iron");
		ORE_DICT_TO_TAG.put("oreGold", "c:ores/gold");
		ORE_DICT_TO_TAG.put("dustRedstone", "c:dusts/redstone");
		ORE_DICT_TO_TAG.put("paper", "c:paper");
		ORE_DICT_TO_TAG.put("leather", "c:leathers");
		ORE_DICT_TO_TAG.put("feather", "c:feathers");
		ORE_DICT_TO_TAG.put("dye", "c:dyes");
		ORE_DICT_TO_TAG.put("record", "c:music_discs");

		// 1.12.2 -> 1.21.1 item name remapping
		ITEM_REMAP.put("minecraft:stonebrick", "minecraft:stone_brick");
		ITEM_REMAP.put("minecraft:brick_block", "minecraft:bricks");
		ITEM_REMAP.put("minecraft:reeds", "minecraft:sugar_cane");
		ITEM_REMAP.put("minecraft:yellow_flower", "minecraft:dandelion");
		ITEM_REMAP.put("minecraft:red_flower", "minecraft:poppy");
		ITEM_REMAP.put("minecraft:fish", "minecraft:cod");
		ITEM_REMAP.put("minecraft:skull", "minecraft:skeleton_skull");
		ITEM_REMAP.put("minecraft:silver_shulker_box", "minecraft:light_gray_shulker_box");
		ITEM_REMAP.put("minecraft:boat", "minecraft:oak_boat");
		ITEM_REMAP.put("minecraft:fence", "minecraft:oak_fence");
		ITEM_REMAP.put("minecraft:fence_gate", "minecraft:oak_fence_gate");
		ITEM_REMAP.put("minecraft:wooden_door", "minecraft:oak_door");
		ITEM_REMAP.put("minecraft:stained_glass", "minecraft:white_stained_glass");
		ITEM_REMAP.put("minecraft:bed", "minecraft:white_bed");
		ITEM_REMAP.put("minecraft:dye", "minecraft:ink_sac");
		ITEM_REMAP.put("minecraft:stone_slab2", "minecraft:red_sandstone_slab");
		ITEM_REMAP.put("minecraft:melon", "minecraft:melon_slice");
		ITEM_REMAP.put("minecraft:melon_block", "minecraft:melon");
		ITEM_REMAP.put("minecraft:slime", "minecraft:slime_ball");
		ITEM_REMAP.put("minecraft:snow_layer", "minecraft:snow");
		ITEM_REMAP.put("minecraft:golden_rail", "minecraft:powered_rail");
		ITEM_REMAP.put("minecraft:lit_pumpkin", "minecraft:jack_o_lantern");
		ITEM_REMAP.put("minecraft:firework_charge", "minecraft:firework_star");
		ITEM_REMAP.put("minecraft:trapdoor", "minecraft:oak_trapdoor");
		ITEM_REMAP.put("minecraft:concrete_powder", "minecraft:white_concrete_powder");
		ITEM_REMAP.put("minecraft:bed", "minecraft:white_bed");
		ITEM_REMAP.put("minecraft:fireworks", "minecraft:firework_rocket");
		ITEM_REMAP.put("minecraft:chorus_fruit_popped", "minecraft:popped_chorus_fruit");
		ITEM_REMAP.put("minecraft:red_nether_brick", "minecraft:red_nether_bricks");
		ITEM_REMAP.put("minecraft:sign", "minecraft:oak_sign");
		ITEM_REMAP.put("minecraft:wooden_button", "minecraft:oak_button");
		ITEM_REMAP.put("minecraft:wooden_pressure_plate", "minecraft:oak_pressure_plate");
		ITEM_REMAP.put("minecraft:speckled_melon", "minecraft:glistering_melon_slice");
		ITEM_REMAP.put("minecraft:end_bricks", "minecraft:end_stone_bricks");
		ITEM_REMAP.put("minecraft:wool", "minecraft:white_wool");
		ITEM_REMAP.put("minecraft:concrete", "minecraft:white_concrete");
		ITEM_REMAP.put("minecraft:stained_hardened_clay", "minecraft:white_terracotta");
		ITEM_REMAP.put("minecraft:stained_glass_pane", "minecraft:white_stained_glass_pane");
		ITEM_REMAP.put("minecraft:hardened_clay", "minecraft:terracotta");
		ITEM_REMAP.put("minecraft:carpet", "minecraft:white_carpet");
		ITEM_REMAP.put("minecraft:banner", "minecraft:white_banner");
		ITEM_REMAP.put("minecraft:noteblock", "minecraft:note_block");
		ITEM_REMAP.put("minecraft:magma", "minecraft:magma_block");
		ITEM_REMAP.put("minecraft:wooden_pickaxe", "minecraft:wooden_pickaxe"); // OK
		ITEM_REMAP.put("minecraft:spruce_door", "minecraft:spruce_door"); // OK
		ITEM_REMAP.put("minecraft:jungle_door", "minecraft:jungle_door"); // OK
		ITEM_REMAP.put("minecraft:acacia_door", "minecraft:acacia_door"); // OK
		ITEM_REMAP.put("minecraft:dark_oak_door", "minecraft:dark_oak_door"); // OK
		ITEM_REMAP.put("minecraft:birch_door", "minecraft:birch_door"); // OK

		// Data-value-dependent remapping
		Map<Integer, String> stonebrickData = new HashMap<>();
		stonebrickData.put(0, "minecraft:stone_bricks");
		stonebrickData.put(1, "minecraft:mossy_stone_bricks");
		stonebrickData.put(2, "minecraft:cracked_stone_bricks");
		stonebrickData.put(3, "minecraft:chiseled_stone_bricks");
		DATA_REMAP.put("minecraft:stonebrick", stonebrickData);

		Map<Integer, String> stoneData = new HashMap<>();
		stoneData.put(0, "minecraft:stone");
		stoneData.put(1, "minecraft:granite");
		stoneData.put(2, "minecraft:polished_granite");
		stoneData.put(3, "minecraft:diorite");
		stoneData.put(4, "minecraft:polished_diorite");
		stoneData.put(5, "minecraft:andesite");
		stoneData.put(6, "minecraft:polished_andesite");
		DATA_REMAP.put("minecraft:stone", stoneData);

		Map<Integer, String> cobblestoneWallData = new HashMap<>();
		cobblestoneWallData.put(0, "minecraft:cobblestone_wall");
		cobblestoneWallData.put(1, "minecraft:mossy_cobblestone_wall");
		DATA_REMAP.put("minecraft:cobblestone_wall", cobblestoneWallData);

		Map<Integer, String> fishData = new HashMap<>();
		fishData.put(0, "minecraft:cod");
		fishData.put(1, "minecraft:salmon");
		fishData.put(2, "minecraft:tropical_fish");
		fishData.put(3, "minecraft:pufferfish");
		DATA_REMAP.put("minecraft:fish", fishData);

		Map<Integer, String> skullData = new HashMap<>();
		skullData.put(0, "minecraft:skeleton_skull");
		skullData.put(1, "minecraft:wither_skeleton_skull");
		skullData.put(2, "minecraft:zombie_head");
		skullData.put(3, "minecraft:player_head");
		skullData.put(4, "minecraft:creeper_head");
		skullData.put(5, "minecraft:dragon_head");
		DATA_REMAP.put("minecraft:skull", skullData);

		Map<Integer, String> dyeData = new HashMap<>();
		dyeData.put(0, "minecraft:ink_sac");
		dyeData.put(1, "minecraft:red_dye");
		dyeData.put(2, "minecraft:green_dye");
		dyeData.put(3, "minecraft:cocoa_beans");
		dyeData.put(4, "minecraft:lapis_lazuli");
		dyeData.put(5, "minecraft:purple_dye");
		dyeData.put(6, "minecraft:cyan_dye");
		dyeData.put(7, "minecraft:light_gray_dye");
		dyeData.put(8, "minecraft:gray_dye");
		dyeData.put(9, "minecraft:pink_dye");
		dyeData.put(10, "minecraft:lime_dye");
		dyeData.put(11, "minecraft:yellow_dye");
		dyeData.put(12, "minecraft:light_blue_dye");
		dyeData.put(13, "minecraft:magenta_dye");
		dyeData.put(14, "minecraft:orange_dye");
		dyeData.put(15, "minecraft:bone_meal");
		DATA_REMAP.put("minecraft:dye", dyeData);

		Map<Integer, String> stainedGlassData = new HashMap<>();
		stainedGlassData.put(0, "minecraft:white_stained_glass");
		stainedGlassData.put(1, "minecraft:orange_stained_glass");
		stainedGlassData.put(2, "minecraft:magenta_stained_glass");
		stainedGlassData.put(3, "minecraft:light_blue_stained_glass");
		stainedGlassData.put(4, "minecraft:yellow_stained_glass");
		stainedGlassData.put(5, "minecraft:lime_stained_glass");
		stainedGlassData.put(6, "minecraft:pink_stained_glass");
		stainedGlassData.put(7, "minecraft:gray_stained_glass");
		stainedGlassData.put(8, "minecraft:light_gray_stained_glass");
		stainedGlassData.put(9, "minecraft:cyan_stained_glass");
		stainedGlassData.put(10, "minecraft:purple_stained_glass");
		stainedGlassData.put(11, "minecraft:blue_stained_glass");
		stainedGlassData.put(12, "minecraft:brown_stained_glass");
		stainedGlassData.put(13, "minecraft:green_stained_glass");
		stainedGlassData.put(14, "minecraft:red_stained_glass");
		stainedGlassData.put(15, "minecraft:black_stained_glass");
		DATA_REMAP.put("minecraft:stained_glass", stainedGlassData);

		Map<Integer, String> bedData = new HashMap<>();
		bedData.put(0, "minecraft:white_bed");
		bedData.put(1, "minecraft:orange_bed");
		bedData.put(2, "minecraft:magenta_bed");
		bedData.put(3, "minecraft:light_blue_bed");
		bedData.put(4, "minecraft:yellow_bed");
		bedData.put(5, "minecraft:lime_bed");
		bedData.put(6, "minecraft:pink_bed");
		bedData.put(7, "minecraft:gray_bed");
		bedData.put(8, "minecraft:light_gray_bed");
		bedData.put(9, "minecraft:cyan_bed");
		bedData.put(10, "minecraft:purple_bed");
		bedData.put(11, "minecraft:blue_bed");
		bedData.put(12, "minecraft:brown_bed");
		bedData.put(13, "minecraft:green_bed");
		bedData.put(14, "minecraft:red_bed");
		bedData.put(15, "minecraft:black_bed");
		DATA_REMAP.put("minecraft:bed", bedData);

		Map<Integer, String> quartzBlockData = new HashMap<>();
		quartzBlockData.put(0, "minecraft:quartz_block");
		quartzBlockData.put(1, "minecraft:chiseled_quartz_block");
		quartzBlockData.put(2, "minecraft:quartz_pillar");
		DATA_REMAP.put("minecraft:quartz_block", quartzBlockData);

		Map<Integer, String> prismarineData = new HashMap<>();
		prismarineData.put(0, "minecraft:prismarine");
		prismarineData.put(1, "minecraft:prismarine_bricks");
		prismarineData.put(2, "minecraft:dark_prismarine");
		DATA_REMAP.put("minecraft:prismarine", prismarineData);

		Map<Integer, String> stoneSlabData = new HashMap<>();
		stoneSlabData.put(0, "minecraft:smooth_stone_slab");
		stoneSlabData.put(1, "minecraft:sandstone_slab");
		stoneSlabData.put(2, "minecraft:petrified_oak_slab");
		stoneSlabData.put(3, "minecraft:cobblestone_slab");
		stoneSlabData.put(4, "minecraft:brick_slab");
		stoneSlabData.put(5, "minecraft:stone_brick_slab");
		stoneSlabData.put(6, "minecraft:nether_brick_slab");
		stoneSlabData.put(7, "minecraft:quartz_slab");
		DATA_REMAP.put("minecraft:stone_slab", stoneSlabData);
	}

	/** Convert an old 1.12.2 item name (and optional data value) to 1.21.1 name. */
	public static String remapItem(String oldItem, int data) {
		Map<Integer, String> dataMap = DATA_REMAP.get(oldItem);
		if (dataMap != null) {
			String remapped = dataMap.get(data);
			if (remapped != null)
				return remapped;
		}
		String simpleRemap = ITEM_REMAP.get(oldItem);
		return simpleRemap != null ? simpleRemap : oldItem;
	}

	private static String oreDictToTag(String ore) {
		String mapped = ORE_DICT_TO_TAG.get(ore);
		if (mapped != null)
			return mapped;
		// Convert camelCase OreDict name to tag path
		StringBuilder path = new StringBuilder();
		for (int i = 0; i < ore.length(); i++) {
			char c = ore.charAt(i);
			if (Character.isUpperCase(c)) {
				path.append('_');
				path.append(Character.toLowerCase(c));
			} else {
				path.append(c);
			}
		}
		String tagPath = path.toString();
		if (tagPath.startsWith("nugget_")) {
			tagPath = "nuggets/" + tagPath.substring("nugget_".length());
		} else if (tagPath.startsWith("ingot_")) {
			tagPath = "ingots/" + tagPath.substring("ingot_".length());
		} else if (tagPath.startsWith("gem_")) {
			tagPath = "gems/" + tagPath.substring("gem_".length());
		} else if (tagPath.startsWith("dust_")) {
			tagPath = "dusts/" + tagPath.substring("dust_".length());
		} else if (tagPath.startsWith("block_")) {
			tagPath = "storage_blocks/" + tagPath.substring("block_".length());
		} else if (tagPath.startsWith("ore_")) {
			tagPath = "ores/" + tagPath.substring("ore_".length());
		}
		return "c:" + tagPath;
	}

	public CompoundTag getItemData(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	@Override
	public boolean isStackOf(ItemStack ingredient, ItemStack stack) {
		return ingredient.getItem() == stack.getItem()
				&& (!ingredient.has(DataComponents.CUSTOM_DATA) || ItemStack.isSameItemSameComponents(ingredient, stack));
	}

	@Override
	public boolean isEqual(ItemStack s1, ItemStack s2) {
		return ItemStack.isSameItemSameComponents(s1, s2);
	}

	@Override
	public ItemPredicate getItemPredicate(JsonElement element, JsonContextPublic context) {
		List<ItemPredicate> predicates = new LinkedList<>();

		if (element.isJsonPrimitive()) {
			String item = element.getAsString();
			if (item.startsWith("#")) {
				ItemPredicate constant = context.getConstant(item.substring(1));
				if (constant == null)
					throw new JsonSyntaxException("Predicate referenced invalid constant: " + item);
				return constant;
			}
			JsonObject object = new JsonObject();
			String remapped = remapItem(item, 0);
			if ("minecraft:tipped_arrow".equals(remapped))
				return new ItemPredicate(Ingredient.of(getTippedArrowStacks()));
			object.addProperty("item", remapped);
			return new ItemIngredient(parseIngredient(object));
		}
		if (element.isJsonArray())
			for (JsonElement json : element.getAsJsonArray())
				predicates.add(getItemPredicate(json, context));
		else if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (!object.has("type") && object.has("item")) {
				String item = GsonHelper.getAsString(object, "item");
				if (item.startsWith("#")) {
					ItemPredicate constant = context.getConstant(item.substring(1));
					if (constant == null)
						throw new JsonSyntaxException("Predicate referenced invalid constant: " + item);
					return constant;
				}
			}
			if (object.has("type")) {
				String typeStr = GsonHelper.getAsString(object, "type");
				if ("forge:ore_dict".equals(typeStr)) {
					String ore = GsonHelper.getAsString(object, "ore");
					String tag = oreDictToTag(ore);
					JsonObject tagObj = new JsonObject();
					tagObj.addProperty("tag", tag);
					return new ItemIngredient(parseIngredient(tagObj));
				}
				ResourceLocation type = ResourceLocation.parse(context.appendModId(typeStr));
				if (REGISTRY.containsKey(type))
					return REGISTRY.get(type).apply(object);
			}
			return new ItemIngredient(parseIngredient(object));
		} else
			throw new JsonSyntaxException("Expected predicate to be an object or an array of objects");

		return new ItemCompound(predicates);
	}

	@Override
	public void registerItemPredicate(ResourceLocation location, ItemPredicate.Factory factory) {
		REGISTRY.put(location, factory);
	}

	@Override
	public ItemStack getParchment(ITechnology tech, Parchment type) {
		ItemStack stack = new ItemStack((type == Parchment.IDEA ? Content.i_parchmentIdea : Content.i_parchmentResearch).get());
		stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
			CompoundTag tag = data.copyTag();
			tag.putString("FTGU", tech.getRegistryName().toString());
			return CustomData.of(tag);
		});
		return stack;
	}

	private static ItemStack[] getTippedArrowStacks() {
		if (TIPPED_ARROW_STACKS == null) {
			List<ItemStack> list = new ArrayList<>();
			for (var entry : BuiltInRegistries.POTION.entrySet()) {
				if (entry.getValue().getEffects().isEmpty())
					continue;
				ItemStack arrow = new ItemStack(Items.TIPPED_ARROW);
				arrow.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(entry.getValue())));
				list.add(arrow);
			}
			TIPPED_ARROW_STACKS = list.toArray(ItemStack[]::new);
		}
		return TIPPED_ARROW_STACKS;
	}

	@Nullable
	@Override
	public Technology getTechnology(ItemStack parchment) {
		CompoundTag tag = getItemData(parchment);
		String ftguTag = tag.getString("FTGU");
		return TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(ftguTag));
	}

	@Override
	public List<BlockSerializable> getInspected(ItemStack inspector) {
		return ItemMagnifyingGlass.getInspected(inspector);
	}

	private static Ingredient parseIngredient(JsonObject object) {
		// Remap item name and strip old data values
		if (object.has("item") && !object.has("type")) {
			String item = GsonHelper.getAsString(object, "item");
			int data = object.has("data") ? GsonHelper.getAsInt(object, "data") : 0;
			String remapped = remapItem(item, data);
			object.addProperty("item", remapped);
			object.remove("data");
		}
		return Ingredient.CODEC.parse(JsonOps.INSTANCE, object).getOrThrow(JsonSyntaxException::new);
	}

}
