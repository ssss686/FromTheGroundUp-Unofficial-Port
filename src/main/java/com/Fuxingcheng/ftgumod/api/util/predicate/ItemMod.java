package com.Fuxingcheng.ftgumod.api.util.predicate;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.registries.BuiltInRegistries;

public class ItemMod extends ItemPredicate {

	private static final Map<String, ItemMod> map = new HashMap<>();

	private final String modid;

	public ItemMod(String modid) {
		super(BuiltInRegistries.ITEM.stream()
				.filter(i -> BuiltInRegistries.ITEM.getKey(i).getNamespace().equals(modid))
				.map(ItemStack::new)
				.toArray(ItemStack[]::new));
		this.modid = modid;
	}

	@Override
	public boolean test(ItemStack item) {
		return BuiltInRegistries.ITEM.getKey(item.getItem()).getNamespace().equals(modid);
	}

	public static class Factory implements ItemPredicate.Factory {

		@Override
		public ItemPredicate apply(JsonObject json) {
			String modid = GsonHelper.getAsString(json, "modid");
			ItemMod item = map.get(modid);
			if (item == null) {
				item = new ItemMod(modid);
				map.put(modid, item);
			}
			return item;
		}

	}

}
