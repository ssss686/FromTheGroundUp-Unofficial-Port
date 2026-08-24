package com.fuxingcheng.fromthegroundup.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeBookServerImpl extends ServerRecipeBook {

	private static final Logger LOGGER = LogManager.getLogger();
	private final ServerPlayer player;

	public RecipeBookServerImpl(ServerPlayer player) {
		this.player = player;
	}

	public void addRecipes(Collection<RecipeHolder<?>> recipes) {
		for (RecipeHolder<?> recipe : recipes)
			super.add(recipe);
	}

}
