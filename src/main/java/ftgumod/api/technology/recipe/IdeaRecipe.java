package ftgumod.api.technology.recipe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ftgumod.api.FTGUAPI;
import ftgumod.api.util.JsonContextPublic;
import ftgumod.api.util.predicate.ItemPredicate;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.NonNullList;
import org.apache.commons.lang3.tuple.Pair;

public class IdeaRecipe implements IIdeaRecipe {

	private final NonNullList<Pair<ItemPredicate, Boolean>> recipe;
	private final int needed;

	public IdeaRecipe(NonNullList<Pair<ItemPredicate, Boolean>> recipe, int needed) {
		this.needed = needed;
		this.recipe = recipe;
	}

	public static IdeaRecipe deserialize(JsonObject object, JsonContextPublic context) {
		int amount = GsonHelper.getAsInt(object, "amount");
		JsonArray ingredients = GsonHelper.getAsJsonArray(object, "ingredients");

		NonNullList<Pair<ItemPredicate, Boolean>> recipe = NonNullList.create();
		for (JsonElement element : ingredients) {
			ItemPredicate predicate = FTGUAPI.stackUtils.getItemPredicate(element, context);
			JsonElement first = element;
			while (first.isJsonArray())
				first = first.getAsJsonArray().get(0);
			recipe.add(Pair.of(predicate,
					first.isJsonObject() && first.getAsJsonObject().has("consume")
							? GsonHelper.getAsBoolean(first.getAsJsonObject(), "consume")
							: null));
		}

		return new IdeaRecipe(recipe, amount);
	}

	@Override
	public NonNullList<ItemStack> test(Container input) {
		NonNullList<ItemStack> remaining = NonNullList.create();
		for (int i = 0; i < input.getContainerSize(); i++)
			remaining.add(input.getItem(i).isEmpty() ? ItemStack.EMPTY : input.getItem(i).copy());

		Set<Pair<ItemPredicate, Boolean>> copy = new HashSet<>(recipe);

		loop: for (int i = 0; i < input.getContainerSize(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty())
				continue;

			Iterator<Pair<ItemPredicate, Boolean>> iterator = copy.iterator();
			while (iterator.hasNext()) {
				Pair<ItemPredicate, Boolean> match = iterator.next();
				if (match.getLeft().test(stack)) {
					iterator.remove();
					if (match.getRight() != null) {
						if (match.getRight())
							remaining.set(i, ItemStack.EMPTY);
						else
							remaining.set(i, stack.copy());
					} else
						remaining.get(i).shrink(1);
					continue loop;
				}
			}
			return null;
		}

		return recipe.size() - copy.size() >= needed ? remaining : null;
	}

}
