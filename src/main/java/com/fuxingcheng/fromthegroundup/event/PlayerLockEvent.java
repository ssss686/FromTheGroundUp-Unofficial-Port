package com.fuxingcheng.fromthegroundup.event;

import javax.annotation.Nullable;

import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PlayerLockEvent {

	private final Player player;
	private final ItemStack stack;
	@Nullable
	private final RecipeHolder<?> recipe;
	private boolean canceled;

	public static final Event<Consumer> EVENT = EventFactory.createArrayBacked(Consumer.class,
			listeners -> event -> {
				for (Consumer listener : listeners) {
					listener.accept(event);
					if (event.isCanceled()) {
						break;
					}
				}
			});

	public PlayerLockEvent(Player player, ItemStack stack, @Nullable RecipeHolder<?> recipe) {
		this.player = player;
		this.stack = stack;
		this.recipe = recipe;
		this.canceled = !TechnologyManager.INSTANCE.isLocked(stack, player);
	}

	public Player getPlayer() {
		return player;
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

	public interface Consumer {
		void accept(PlayerLockEvent event);
	}

}
