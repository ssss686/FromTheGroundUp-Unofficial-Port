package ftgumod.api.util.predicate;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class BlockPredicateCompound extends BlockPredicate {

	private final Iterable<BlockPredicate> compound;

	public BlockPredicateCompound(Iterable<BlockPredicate> compound) {
		super(null, null, null);
		this.compound = compound;
	}

	public static BlockPredicate deserialize(JsonElement element) {
		if (element.isJsonArray()) {
			Set<BlockPredicate> compound = new HashSet<>();
			for (JsonElement e : element.getAsJsonArray())
				compound.add(deserialize(e));
			return new BlockPredicateCompound(compound);
		} else if (element.isJsonObject())
			return BlockPredicate.deserialize(element.getAsJsonObject());
		else throw new JsonSyntaxException("Expected decipher to be an object or an array of objects");
	}

	@Override
	public boolean test(ServerLevel world, BlockPos pos, BlockState state) {
		for (BlockPredicate predicate : compound)
			if (predicate.test(world, pos, state))
				return true;
		return false;
	}

	@Override
	public boolean test(ServerLevel world, BlockPos pos, Block block, Map<Property<?>, Object> properties) {
		for (BlockPredicate predicate : compound)
			if (predicate.test(world, pos, block, properties))
				return true;
		return false;
	}

}
