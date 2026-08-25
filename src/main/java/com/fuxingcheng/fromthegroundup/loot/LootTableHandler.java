package com.fuxingcheng.fromthegroundup.loot;

import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetCustomDataFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.nbt.CompoundTag;

public class LootTableHandler {

	// Vanilla loot table IDs
	private static final ResourceLocation VILLAGE_ARMORER = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/village/village_armorer");
	private static final ResourceLocation STRONGHOLD_LIBRARY = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_library");
	private static final ResourceLocation DESERT_PYRAMID = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/desert_pyramid");

	public static void register() {
		LootTableEvents.MODIFY.register((id, tableBuilder, source) -> {
			if (VILLAGE_ARMORER.equals(id)) {
				addParchmentPool(tableBuilder, "ftgumod:survival/refinement", 1, 3);
				addParchmentPool(tableBuilder, "ftgumod:survival/smithing", 1, 7);
				addParchmentPool(tableBuilder, "ftgumod:survival/metal_armor", 1, 7);
				addParchmentPool(tableBuilder, "ftgumod:survival/lapidary", 1, 15);
			} else if (STRONGHOLD_LIBRARY.equals(id)) {
				addParchmentPool(tableBuilder, "ftgumod:magic/enchanting", 1, 3);
				addParchmentPool(tableBuilder, "ftgumod:magic/brewing", 1, 7);
			} else if (DESERT_PYRAMID.equals(id)) {
				addParchmentPool(tableBuilder, "ftgumod:survival/stoneworking", 1, 3);
				addParchmentPool(tableBuilder, "ftgumod:combat/archery", 1, 7);
			}
		});

		FromTheGroundUp.LOGGER.info("Registered loot table modifications");
	}

	private static void addParchmentPool(LootTable.Builder tableBuilder, String techId, int weight, int emptyWeight) {
		CompoundTag tag = new CompoundTag();
		tag.putString("FTGU", techId);

		LootPool pool = LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1))
				.add(LootItem.lootTableItem(Content.i_parchmentResearch)
						.setWeight(weight)
						.apply(SetCustomDataFunction.setCustomData(tag)))
				.add(EmptyLootItem.emptyItem()
						.setWeight(emptyWeight))
				.build();

		tableBuilder.pool(pool);
	}
}
