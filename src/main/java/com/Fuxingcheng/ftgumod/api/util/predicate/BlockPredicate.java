package com.Fuxingcheng.ftgumod.api.util.predicate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicate {

	private static final Map<String, String> BLOCK_REMAP = new HashMap<>();

	static {
		BLOCK_REMAP.put("minecraft:bed", "minecraft:white_bed");
	}

	public static final BlockPredicate ANY = new BlockPredicate(null, null, null, null);

	private final Block block;
	private final Map<Property<?>, Object> properties;
	private final LocationPredicate location;
	private final TagKey<Block> tag;

	public BlockPredicate(Block block, Map<Property<?>, Object> properties, LocationPredicate location) {
		this(block, properties, location, null);
	}

	public BlockPredicate(Block block, Map<Property<?>, Object> properties, LocationPredicate location, TagKey<Block> tag) {
		this.block = block;
		this.properties = properties;
		this.location = location;
		this.tag = tag;
	}

	public static BlockPredicate deserialize(JsonObject object) {
		if (!object.has("block") && !object.has("state") && !object.has("location") && !object.has("tag"))
			return ANY;

		Block block = null;
		if (object.has("block")) {
			String blockName = GsonHelper.getAsString(object, "block");
			String remapped = BLOCK_REMAP.getOrDefault(blockName, blockName);
			ResourceLocation location = ResourceLocation.tryParse(remapped);
			if (location == null || !BuiltInRegistries.BLOCK.containsKey(location))
				throw new JsonSyntaxException("Unknown block type '" + remapped + "'");
			block = BuiltInRegistries.BLOCK.get(location);
		}

		TagKey<Block> tag = null;
		if (object.has("tag")) {
			ResourceLocation location = ResourceLocation.tryParse(GsonHelper.getAsString(object, "tag"));
			if (location == null)
				throw new JsonSyntaxException("Invalid tag name");
			tag = TagKey.create(Registries.BLOCK, location);
		}

		Map<Property<?>, Object> properties = null;
		if (object.has("state")) {
			if (block == null)
				throw new JsonSyntaxException("Can't define block state without a specific block type");

			StateDefinition<Block, BlockState> blockState = block.getStateDefinition();

			for (var entry : GsonHelper.getAsJsonObject(object, "state").entrySet()) {
				Property<?> property = blockState.getProperty(entry.getKey());
				if (property == null)
					throw new JsonSyntaxException("Unknown block state property '" + entry.getKey()
							+ "' for block '" + BuiltInRegistries.BLOCK.getKey(block) + "'");

				String name = GsonHelper.convertToString(entry.getValue(), entry.getKey());
				Optional<?> opt = property.getValue(name);
				if (opt.isEmpty())
					throw new JsonSyntaxException("Invalid block state value '" + name + "' for property '"
							+ entry.getKey() + "' on block '" + BuiltInRegistries.BLOCK.getKey(block) + "'");

				if (properties == null)
					properties = new HashMap<>();
				properties.put(property, opt.get());
			}
		}

		LocationPredicate location = null;
		if (object.has("location")) {
			// LocationPredicate in 1.21.1 uses codec; try to parse from JSON
			var result = LocationPredicate.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,
					object.get("location")).result();
			if (result.isPresent())
				location = result.get();
		}

		return new BlockPredicate(block, properties, location, tag);
	}

	public boolean test(ServerLevel world, BlockPos pos, BlockState state) {
		if (this == ANY)
			return true;
		if (this.block != null && state.getBlock() != this.block)
			return false;
		if (this.tag != null && !state.is(this.tag))
			return false;
		if (this.properties != null)
			for (var entry : this.properties.entrySet())
				if (state.getValue(entry.getKey()) != entry.getValue())
					return false;
		return location == null || location.matches(world, pos.getX(), pos.getY(), pos.getZ());
	}

	public boolean test(ServerLevel world, BlockPos pos, Block block, Map<Property<?>, Object> properties) {
		if (this == ANY)
			return true;
		if (this.block != null && block != this.block)
			return false;
		if (this.tag != null && !world.getBlockState(pos).is(this.tag))
			return false;
		if (this.properties != null)
			for (var entry : this.properties.entrySet())
				if (properties.get(entry.getKey()) != entry.getValue())
					return false;
		return location == null || location.matches(world, pos.getX(), pos.getY(), pos.getZ());
	}

}
