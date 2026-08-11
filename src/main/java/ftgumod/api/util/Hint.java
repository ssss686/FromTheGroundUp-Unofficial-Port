package ftgumod.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import ftgumod.api.util.predicate.BlockPredicate;
import ftgumod.api.util.predicate.BlockPredicateCompound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.ChatFormatting;
import org.apache.commons.lang3.tuple.Pair;

public class Hint {

	private final Component hint;
	private final List<Pair<BlockPredicate, Component>> hints;

	public Hint(Component hint, List<Pair<BlockPredicate, Component>> hints) {
		this.hint = hint;
		this.hints = hints;
	}

	public static Hint deserialize(JsonElement eHint, JsonElement decipher) {
		Component hint = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, eHint).result().orElse(Component.empty());
		if (decipher != null) {
			if (decipher.isJsonArray()) {
				List<Pair<BlockPredicate, Component>> hints = new ArrayList<>();
				for (JsonElement e : decipher.getAsJsonArray()) {
					if (!e.isJsonObject())
						throw new JsonSyntaxException("Expected decipher to be an object or an array of objects");
					BlockPredicate predicate = BlockPredicateCompound.deserialize(e.getAsJsonObject().get("decipher"));
					Component newHint = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, e.getAsJsonObject().get("hint")).result().orElse(Component.empty());
					hints.add(Pair.of(predicate, newHint));
				}
				return new Hint(hint, hints);
			}
			if (!decipher.isJsonObject())
				throw new JsonSyntaxException("Expected decipher to be an object or an array of objects");
			if (decipher.getAsJsonObject().has("decipher")) {
				BlockPredicate predicate = BlockPredicateCompound
						.deserialize(decipher.getAsJsonObject().get("decipher"));
				Component newHint = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, decipher.getAsJsonObject().get("hint")).result().orElse(Component.empty());
				List<Pair<BlockPredicate, Component>> hints = Collections
						.singletonList(Pair.of(predicate, newHint));
				return new Hint(hint, hints);
			}
		}
		return new Hint(hint, Collections.emptyList());
	}

	public Component getHint(List<BlockSerializable> inspected) {
		Component text = this.hint.copy();
		for (Pair<BlockPredicate, Component> hint : hints) {
			for (BlockSerializable block : inspected) {
				if (block.test(hint.getLeft())) {
					Component sibling = hint.getRight().copy().withStyle(ChatFormatting.YELLOW);
					text = Component.literal("").append(text).append("\n - ").append(sibling);
				}
			}
		}
		return text;
	}

	public Component getObfuscatedHint() {
		return this.hint.copy().withStyle(style -> style.withObfuscated(true));
	}

	public boolean inspect(BlockSerializable block, List<BlockSerializable> inspected) {
		for (Pair<BlockPredicate, Component> hint : hints) {
			if (block.test(hint.getLeft())) {
				boolean already = false;
				for (BlockSerializable b : inspected) {
					if (b.test(hint.getLeft())) {
						already = true;
						break;
					}
				}
				if (!already)
					return true;
			}
		}
		return false;
	}

}
