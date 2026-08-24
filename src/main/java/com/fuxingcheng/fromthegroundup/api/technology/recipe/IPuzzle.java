package com.fuxingcheng.fromthegroundup.api.technology.recipe;

import java.util.List;

import javax.annotation.Nullable;

import com.fuxingcheng.fromthegroundup.api.inventory.ContainerResearch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public interface IPuzzle {

	Tag write(HolderLookup.Provider registries);

	void read(Tag tag, HolderLookup.Provider registries);

	IResearchRecipe getRecipe();

	boolean test();

	void onStart(ContainerResearch container);

	void onInventoryChange(ContainerResearch container);

	void onFinish();

	void onRemove(@Nullable Player player, Level world, BlockPos pos);

	void setHints(List<Component> hints);

	List<Component> getHints();

	Object getGui();

}
