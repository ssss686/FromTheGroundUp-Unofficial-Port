package ftgumod.api.technology.puzzle;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import ftgumod.api.FTGUAPI;
import ftgumod.api.inventory.ContainerResearch;
import ftgumod.api.inventory.InventoryCraftingPersistent;
import ftgumod.api.inventory.SlotCrafting;
import ftgumod.api.technology.puzzle.gui.PuzzleGuiConnect;
import ftgumod.api.technology.recipe.IPuzzle;
import ftgumod.api.util.predicate.ItemPredicate;
import ftgumod.inventory.ContainerResearchTable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class PuzzleConnect implements IPuzzle {

	private final List<ContainerResearch> registry = new LinkedList<>();
	private final ResearchConnect research;
	private ContainerResearchTable container;

	public PuzzleConnect(ResearchConnect research) {
		this.research = research;
	}

	private Container inventory() {
		return new InventoryCraftingPersistent(container.invInput, 3, 3, 1);
	}

	@Override
	public Tag write(HolderLookup.Provider registries) {
		ListTag items = new ListTag();
		Container inv = inventory();
		for (int i = 0; i < 3; ++i) {
			if (!inv.getItem(i).isEmpty()) {
				CompoundTag compound = new CompoundTag();
				compound.putByte("Slot", (byte) i);
				inv.getItem(i).save(registries, compound);
				items.add(compound);
			}
		}
		return items;
	}

	@Override
	public void read(Tag tag, HolderLookup.Provider registries) {
		ListTag items = (ListTag) tag;
		Container inv = inventory();
		for (int i = 0; i < items.size(); ++i) {
			CompoundTag compound = items.getCompound(i);
			byte slot = compound.getByte("Slot");
			if (slot >= 0 && slot < 3)
				inv.setItem(slot, ItemStack.parseOptional(registries, compound));
		}
	}

	@Override
	public ResearchConnect getRecipe() {
		return research;
	}

	private static boolean connects(ItemPredicate predicate, ItemStack stack) {
		if (predicate.getMatchingStacks().length == 0)
			return true;

		var server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return false;
		var registryAccess = server.registryAccess();
		var recipeManager = server.getRecipeManager();

		for (RecipeHolder<?> holder : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
			ItemStack result = holder.value().getResultItem(registryAccess);
			if (predicate.test(result)) {
				for (Ingredient ingredient : holder.value().getIngredients())
					if (ingredient.test(stack))
						return true;
			} else if (FTGUAPI.stackUtils.isEqual(result, stack)) {
				for (Ingredient ingredient : holder.value().getIngredients())
					for (ItemStack s : predicate.getMatchingStacks())
						if (ingredient.test(s))
							return true;
			}
		}

		List<RecipeType<?>> smeltTypes = List.of(RecipeType.SMELTING, RecipeType.BLASTING, RecipeType.SMOKING);
		for (RecipeType<?> smeltType : smeltTypes) {
			for (RecipeHolder<?> holder : getAllSmeltingRecipes(recipeManager, smeltType)) {
				ItemStack result = holder.value().getResultItem(registryAccess);
				for (Ingredient ingredient : holder.value().getIngredients()) {
					ItemStack[] inputs = ingredient.getItems();
					if (predicate.test(result)) {
						for (ItemStack input : inputs)
							if (FTGUAPI.stackUtils.isStackOf(input, stack))
								return true;
					}
					if (FTGUAPI.stackUtils.isEqual(result, stack)) {
						for (ItemStack s : predicate.getMatchingStacks())
							for (ItemStack input : inputs)
								if (FTGUAPI.stackUtils.isStackOf(input, s))
									return true;
					}
				}
			}
		}

		return false;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Collection<RecipeHolder<?>> getAllSmeltingRecipes(RecipeManager rm, RecipeType<?> type) {
		return (Collection) rm.getAllRecipesFor((RecipeType) type);
	}

	@Override
	public void onStart(ContainerResearch container) {
		registry.add(container);
		this.container = (ContainerResearchTable) container;
	}

	@Override
	public boolean test() {
		Container inv = inventory();
		for (int i = 0; i < 3; i++)
			if (inv.getItem(i).isEmpty())
				return false;
		return true;
	}

	public boolean canPlaceInSlot(ItemStack stack, int index) {
		return fits(stack, index);
	}

	private boolean fits(ItemStack stack, int index) {
		Container inv = inventory();
		if (research.left.test(stack) || research.right.test(stack))
			return false;
		for (int i = 0; i < 3; i++)
			if (FTGUAPI.stackUtils.isEqual(stack, inv.getItem(i)))
				return false;

		ItemPredicate left = index > 0 ? new ItemPredicate(inv.getItem(index - 1)) : research.left;
		ItemPredicate right = index < 2 ? new ItemPredicate(inv.getItem(index + 1)) : research.right;
		return connects(left, stack) && connects(right, stack);
	}

	@Override
	public void onInventoryChange(ContainerResearch container) {
		if (!container.isClient())
			container.setChanged();
	}

	@Override
	public void onFinish() {
		Container inv = inventory();
		for (int i = 0; i < 3; i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty())
				inv.setItem(i, stack.copy());
		}
	}

	@Override
	public void onRemove(@Nullable Player player, Level world, BlockPos pos) {
		if (player != null && world != null && !world.isClientSide) {
			Container inv = inventory();
			for (int i = 0; i < 3; i++) {
				ItemStack stack = inv.getItem(i);
				if (!stack.isEmpty() && !player.addItem(stack))
					player.drop(stack, false);
			}
		} else if (world != null)
			Containers.dropContents(world, pos, inventory());

		registry.clear();
	}

	@Override
	public void setHints(List<Component> hints) {
	}

	@Override
	public List<Component> getHints() {
		return Collections.emptyList();
	}

	@Override
	public Object getGui() {
		return new PuzzleGuiConnect(research);
	}

}
