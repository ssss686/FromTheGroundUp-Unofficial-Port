package ftgumod.api.technology;

import java.util.Map;
import java.util.Set;

import ftgumod.api.technology.recipe.IIdeaRecipe;
import ftgumod.api.technology.recipe.IResearchRecipe;
import ftgumod.api.technology.unlock.IUnlock;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public interface ITechnology {

	boolean canCopy();

	boolean researchedAtStart();

	Set<ITechnology> getChildren();

	boolean hasIdeaRecipe();

	IIdeaRecipe getIdeaRecipe();

	boolean hasResearchRecipe();

	IResearchRecipe getResearchRecipe();

	boolean isRoot();

	boolean hasParent();

	ITechnology getParent();

	NonNullList<IUnlock> getUnlock();

	boolean hasCustomUnlock();

	Map<String, Criterion<?>> getCriteria();

	String[][] getRequirements();

	boolean grantCriterion(Player player, String criterion);

	boolean revokeCriterion(Player player, String criterion);

	void setResearched(Player player, boolean announce);

	void removeResearched(Player player);

	DisplayInfo getDisplayInfo();

	Component getDisplayText();

	boolean isResearched(Player player);

	boolean isUnlocked(Player player);

	boolean canResearch(Player player);

	ITechnologyBuilder toBuilder();

	String getGameStage();

	ResourceLocation getRegistryName();

}
