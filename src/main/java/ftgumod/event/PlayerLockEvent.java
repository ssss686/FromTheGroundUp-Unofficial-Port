package ftgumod.event;

import javax.annotation.Nullable;

import ftgumod.technology.TechnologyManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerLockEvent extends PlayerEvent implements ICancellableEvent {

	private final ItemStack stack;
	@Nullable
	private final RecipeHolder<?> recipe;

	public PlayerLockEvent(Player player, ItemStack stack, @Nullable RecipeHolder<?> recipe) {
		super(player);
		this.stack = stack;
		this.recipe = recipe;
		setCanceled(!TechnologyManager.INSTANCE.isLocked(stack, player));
	}

	public ItemStack getStack() {
		return stack;
	}

	@Nullable
	public RecipeHolder<?> getRecipe() {
		return recipe;
	}

}
