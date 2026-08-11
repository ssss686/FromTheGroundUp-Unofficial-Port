package ftgumod.api.technology.puzzle;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ftgumod.api.FTGUAPI;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.util.BlockSerializable;
import ftgumod.api.util.JsonContextPublic;
import ftgumod.api.util.predicate.ItemPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

public class ResearchConnect implements IResearchRecipe {

	public final ItemPredicate left;
	public final ItemPredicate right;
	private ITechnology technology;

	public ResearchConnect(ItemPredicate left, ItemPredicate right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public boolean inspect(BlockSerializable block, List<BlockSerializable> inspected) {
		return false;
	}

	@Override
	public IPuzzle createInstance() {
		return new PuzzleConnect(this);
	}

	@Override
	public ITechnology getTechnology() {
		return technology;
	}

	@Override
	public void setTechnology(ITechnology tech) {
		this.technology = tech;
	}

	public static class Factory implements IResearchRecipe.Factory<ResearchConnect> {

		private static ItemPredicate getStack(JsonElement json, String name, JsonContextPublic context) {
			if (json.isJsonPrimitive()) {
				String item = GsonHelper.convertToString(json, name);
				if (item.startsWith("#")) {
					ItemPredicate constant = context.getConstant(item.substring(1));
					if (constant == null)
						throw new JsonSyntaxException("Predicate referenced invalid constant: " + item);
					return constant;
				}
				JsonObject object = new JsonObject();
				object.addProperty("item", item);
				return FTGUAPI.stackUtils.getItemPredicate(object, context);
			} else {
				return FTGUAPI.stackUtils.getItemPredicate(json.getAsJsonObject(), context);
			}
		}

		@Override
		public ResearchConnect deserialize(JsonObject object, JsonContextPublic context, ResourceLocation technology) {
			JsonElement left = object.get("left");
			if (left == null)
				throw new JsonSyntaxException("Missing left, expected to find a string or a JsonObject");
			JsonElement right = object.get("right");
			if (right == null)
				throw new JsonSyntaxException("Missing right, expected to find a string or a JsonObject");
			return new ResearchConnect(getStack(left, "left", context), getStack(right, "right", context));
		}

	}

}
