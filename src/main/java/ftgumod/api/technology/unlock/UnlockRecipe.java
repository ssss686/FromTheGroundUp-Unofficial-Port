package ftgumod.api.technology.unlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ftgumod.api.FTGUAPI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class UnlockRecipe implements IUnlock {

	private final Ingredient recipe;

	public UnlockRecipe(Ingredient recipe) {
		this.recipe = recipe;
	}

	@Override
	public boolean isDisplayed() {
		return recipe.getItems().length > 0;
	}

	@Override
	public Ingredient getIcon() {
		return recipe;
	}

	@Override
	public boolean unlocks(ItemStack stack) {
		return recipe.test(stack);
	}

	public Collection<RecipeHolder<?>> getRecipeList() {
		List<RecipeHolder<?>> recipes = new ArrayList<>();
		RecipeManager manager = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
		for (RecipeHolder<?> holder : manager.getRecipes()) {
			if (unlocks(holder.value().getResultItem(
					ServerLifecycleHooks.getCurrentServer().registryAccess())))
				recipes.add(holder);
		}
		return recipes;
	}

	@Override
	public void unlock(ServerPlayer player) {
		FTGUAPI.technologyManager.addRecipes(getRecipeList(), player);
	}

	@Override
	public void lock(ServerPlayer player) {
		for (RecipeHolder<?> holder : getRecipeList())
			player.getRecipeBook().remove(holder);
	}

}
