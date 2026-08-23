package com.Fuxingcheng.ftgumod.api.technology;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.Fuxingcheng.ftgumod.api.technology.recipe.IResearchRecipe;
import com.Fuxingcheng.ftgumod.api.technology.unlock.IUnlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.ResourceLocation;

public interface ITechnologyManager {

	boolean isLocked(ItemStack stack, @Nullable Player player);

	void removeCallback(Predicate<? super ITechnology> predicate);

	void addCallback(Consumer<? super ITechnology> action);

	void createCallback(Runnable action);

	void register(ITechnology value);

	void registerAll(ITechnology... values);

	boolean contains(ResourceLocation key);

	boolean contains(ITechnology value);

	ITechnology getTechnology(ResourceLocation key);

	Collection<ITechnology> getTechnologies();

	Set<ResourceLocation> getRegistryNames();

	ITechnologyBuilder createBuilder(ResourceLocation id);

	void sync(ServerPlayer player, ITechnology... toasts);

	void addRecipes(Collection<RecipeHolder<?>> recipes, ServerPlayer player);

	void registerUnlock(ResourceLocation name, IUnlock.Factory<?> factory);

	void registerPuzzle(ResourceLocation name, IResearchRecipe.Factory<?> factory);

}
