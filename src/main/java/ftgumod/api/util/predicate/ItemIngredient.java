package ftgumod.api.util.predicate;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemIngredient extends ItemPredicate {

	private final Ingredient ingredient;

	public ItemIngredient(Ingredient ingredient) {
		super(ingredient);
		this.ingredient = ingredient;
	}

	@Override
	public ItemStack[] getMatchingStacks() {
		return ingredient.getItems();
	}

	@Override
	public boolean test(ItemStack stack) {
		return ingredient.test(stack);
	}

}
