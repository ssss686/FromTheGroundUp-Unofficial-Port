package com.Fuxingcheng.ftgumod.api.technology.unlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.Fuxingcheng.ftgumod.api.FTGUAPI;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

public class UnlockRecipe implements IUnlock {

	private final Ingredient recipe;

	public UnlockRecipe(Ingredient recipe) {
		this.recipe = recipe;
	}

	@Override
	public boolean isDisplayed() {
		if (recipe == null || recipe.isEmpty()) return false;
		try {
			ItemStack[] stacks = recipe.getItems();
			return stacks != null && stacks.length > 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Ingredient getIcon() {
		return recipe;
	}

	@Override
	public boolean unlocks(ItemStack stack) {
		if (recipe == null || recipe.isEmpty()) return false;
		try {
			return recipe.test(stack);
		} catch (Exception e) {
			return false;
		}
	}

	public Collection<RecipeHolder<?>> getRecipeList() {
		List<RecipeHolder<?>> recipes = new ArrayList<>();
		MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
		if (server == null) return recipes;

		RecipeManager manager = server.getRecipeManager();
		for (RecipeHolder<?> holder : manager.getRecipes()) {
			if (unlocks(holder.value().getResultItem(server.registryAccess())))
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