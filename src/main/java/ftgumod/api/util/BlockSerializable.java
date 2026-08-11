package ftgumod.api.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import javax.annotation.Nullable;

import ftgumod.api.util.predicate.BlockPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class BlockSerializable {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final ResourceKey<Level> dimension;
	private final BlockPos pos;
	private final Block block;
	private final Map<Property<?>, Object> properties;

	private final ItemStack display;

	public BlockSerializable(Level world, BlockPos pos, BlockState state, @Nullable ItemStack display) {
		this.dimension = world.dimension();
		this.pos = pos;
		this.block = state.getBlock();
		this.properties = new HashMap<>();

		for (Property<?> property : state.getProperties())
			properties.put(property, state.getValue(property));

		if (display == null || display.isEmpty())
			this.display = new ItemStack(block);
		else
			this.display = display;
	}

	public BlockSerializable(CompoundTag compound) {
		this.dimension = ResourceKey.create(ResourceKey.createRegistryKey(
				ResourceLocation.parse("dimension")),
				ResourceLocation.parse(compound.getString("dimension")));
		this.pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
		this.block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(compound.getString("block")));
		this.properties = new HashMap<>();

		CompoundTag state = compound.getCompound("state");
		StateDefinition<Block, BlockState> container = block.getStateDefinition();

		for (String name : state.getAllKeys()) {
			Property<?> property = container.getProperty(name);
			if (property == null) {
				LOGGER.warn("BlockSerializable: unknown property '{}' for block '{}', skipping", name, compound.getString("block"));
				continue;
			}
			String valueName = state.getString(name);
			var value = property.getValue(valueName);
			if (value.isEmpty()) {
				LOGGER.warn("BlockSerializable: invalid value '{}' for property '{}' on block '{}', skipping", valueName,
						name, compound.getString("block"));
				continue;
			}
			properties.put(property, value.get());
		}

		var server = ServerLifecycleHooks.getCurrentServer();
		RegistryAccess registryAccess = server != null
				? server.registryAccess()
				: RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		display = ItemStack.parseOptional(registryAccess, compound.getCompound("display"));
	}

	public CompoundTag serialize() {
		CompoundTag compound = new CompoundTag();
		compound.putString("dimension", dimension.location().toString());
		compound.putInt("x", pos.getX());
		compound.putInt("y", pos.getY());
		compound.putInt("z", pos.getZ());
		compound.putString("block", BuiltInRegistries.BLOCK.getKey(block).toString());

		CompoundTag state = new CompoundTag();
		for (Map.Entry<Property<?>, Object> entry : properties.entrySet())
			state.putString(entry.getKey().getName(), getPropertyName(entry.getKey(), entry.getValue()));

		compound.put("state", state);
		compound.put("display", display.save(ServerLifecycleHooks.getCurrentServer().registryAccess()));

		return compound;
	}

	@SuppressWarnings("unchecked")
	private <T extends Comparable<T>> String getPropertyName(Property<T> property, Object object) {
		return property.getName((T) object);
	}

	public String getLocalizedName() {
		return display.getDisplayName().getString();
	}

	public boolean test(BlockPredicate predicate) {
		var server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return false;
		ServerLevel level = server.getLevel(dimension);
		if (level == null)
			return false;
		return predicate.test(level, pos, block, properties);
	}

}
