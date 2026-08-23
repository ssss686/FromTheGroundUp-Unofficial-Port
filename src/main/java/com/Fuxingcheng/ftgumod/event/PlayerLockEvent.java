package com.Fuxingcheng.ftgumod.event;

import javax.annotation.Nullable;

import com.Fuxingcheng.ftgumod.technology.TechnologyManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerLockEvent extends PlayerEvent {

	private final ItemStack stack;
	@Nullable
	private final RecipeHolder<?> recipe;
	private boolean canceled;

	public PlayerLockEvent(Player player, ItemStack stack, @Nullable RecipeHolder<?> recipe) {
		super(player);
		this.stack = stack;
		this.recipe = recipe;
		this.canceled = !TechnologyManager.INSTANCE.isLocked(stack, player);
	}

	public ItemStack getStack() {
		return stack;
	}

	@Nullable
	public RecipeHolder<?> getRecipe() {
		return recipe;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}