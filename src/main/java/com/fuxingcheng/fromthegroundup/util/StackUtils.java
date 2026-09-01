package com.fuxingcheng.fromthegroundup.util;
import com.fuxingcheng.fromthegroundup.util.ServerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.api.FTGUAPI;
import com.fuxingcheng.fromthegroundup.api.technology.ITechnology;
import com.fuxingcheng.fromthegroundup.api.util.BlockSerializable;
import com.fuxingcheng.fromthegroundup.api.util.IStackUtils;
import com.fuxingcheng.fromthegroundup.api.util.JsonContextPublic;
import com.fuxingcheng.fromthegroundup.api.util.predicate.ItemCompound;
import com.fuxingcheng.fromthegroundup.api.util.predicate.ItemIngredient;
import com.fuxingcheng.fromthegroundup.api.util.predicate.ItemPredicate;
import com.fuxingcheng.fromthegroundup.item.ItemMagnifyingGlass;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
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

	private static ItemStack[] TIPPED_ARROW_STACKS = null;


	static {
		FTGUAPI.stackUtils = INSTANCE;
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
			JsonObject object = new JsonObject();
			if ("minecraft:tipped_arrow".equals(item))
				return new ItemPredicate(Ingredient.of(getTippedArrowStacks()));
			object.addProperty("item", item);
			return new ItemIngredient(parseIngredient(object));
		}
		if (element.isJsonArray())
			for (JsonElement json : element.getAsJsonArray())
				predicates.add(getItemPredicate(json, context));
		else if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("type")) {
				String typeStr = GsonHelper.getAsString(object, "type");
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
		ItemStack stack = new ItemStack(type == Parchment.IDEA ? Content.i_parchmentIdea : Content.i_parchmentResearch);
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
		if (!tag.contains("FTGU", CompoundTag.TAG_STRING))
			return null;
		String ftguTag = tag.getString("FTGU");
		if (ftguTag.isEmpty())
			return null;
		return TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(ftguTag));
	}

	@Override
	public List<BlockSerializable> getInspected(ItemStack inspector) {
		return ItemMagnifyingGlass.getInspected(inspector);
	}

	private static Ingredient parseIngredient(JsonObject object) {
		if (object.has("item") && !object.has("type")) {
			String item = GsonHelper.getAsString(object, "item");
			ResourceLocation itemId = ResourceLocation.parse(item);
			if (BuiltInRegistries.ITEM.containsKey(itemId)) {
				return Ingredient.of(new ItemStack(BuiltInRegistries.ITEM.get(itemId)));
			}
		}
		com.mojang.serialization.DynamicOps<JsonElement> ops;
		var server = ServerHelper.getCurrentServer();
		if (server != null)
			ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
		else
			ops = JsonOps.INSTANCE;
		return Ingredient.CODEC.parse(ops, object).getOrThrow(JsonSyntaxException::new);
	}

}
