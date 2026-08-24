package com.fuxingcheng.fromthegroundup.technology;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.FTGU;
import com.fuxingcheng.fromthegroundup.api.technology.ITechnology;
import com.fuxingcheng.fromthegroundup.util.StackUtils;
import com.fuxingcheng.fromthegroundup.api.technology.ITechnologyBuilder;
import com.fuxingcheng.fromthegroundup.api.technology.recipe.IIdeaRecipe;
import com.fuxingcheng.fromthegroundup.criterion.TriggerFTGU;
import com.fuxingcheng.fromthegroundup.api.technology.recipe.IResearchRecipe;
import com.fuxingcheng.fromthegroundup.api.technology.recipe.IdeaRecipe;
import com.fuxingcheng.fromthegroundup.api.technology.unlock.IUnlock;
import com.fuxingcheng.fromthegroundup.api.util.JsonContextPublic;
import com.fuxingcheng.fromthegroundup.event.TechnologyEvent;
import com.fuxingcheng.fromthegroundup.util.ListenerTechnology;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import com.fuxingcheng.fromthegroundup.event.TechnologyEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Technology implements ITechnology {

	private static final Logger LOGGER = LogManager.getLogger();

	private final Set<Technology> children = new HashSet<>();
	private final ResourceLocation id;
	private final int level;

	Component displayText;
	DisplayInfo display;
	NonNullList<IUnlock> unlock;
	Technology parent;

	AdvancementRewards rewards;
	Map<String, Criterion<?>> criteria;
	String[][] requirements;

	IIdeaRecipe idea;
	IResearchRecipe research;

	String stage;

	boolean start;
	boolean copy;

	Technology(ResourceLocation id, @Nullable Technology parent, DisplayInfo display, AdvancementRewards rewards,
			Map<String, Criterion<?>> criteria, String[][] requirements, boolean start, boolean copy,
			@Nullable NonNullList<IUnlock> unlock, @Nullable IIdeaRecipe idea, @Nullable IResearchRecipe research,
			String stage) {
		this.id = id;
		this.parent = parent;
		this.display = display;

		this.start = start;
		this.copy = copy;

		this.rewards = rewards;
		this.criteria = criteria;
		this.requirements = requirements;

		this.unlock = unlock == null ? NonNullList.create() : unlock;
		this.idea = idea;
		this.research = research;

		this.stage = stage;

		if (parent == null)
			level = 1;
		else
			level = parent.level + 1;

		updateDisplayText();
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	void updateDisplayText() {
		MutableComponent text = Component.literal("[");
		Style frameStyle = Style.EMPTY.applyFormat(display.getType().getChatColor());
		text = text.withStyle(frameStyle);
		MutableComponent itextcomponent = display.getTitle().copy();
		MutableComponent itextcomponent1 = Component.literal("");
		MutableComponent itextcomponent2 = itextcomponent.copy();
		itextcomponent2 = itextcomponent2.withStyle(frameStyle);
		itextcomponent1.append(itextcomponent2);
		itextcomponent1.append(Component.literal("\n"));
		itextcomponent1.append(display.getDescription());
		itextcomponent = itextcomponent.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1)));
		text.append(itextcomponent);
		text.append(Component.literal("]"));
		this.displayText = text;
	}


	@Override
	public boolean canCopy() {
		return copy;
	}

	@Override
	public boolean researchedAtStart() {
		return start;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<ITechnology> getChildren() {
		return (Set) children;
	}

	public void getChildren(Collection<Technology> collection, boolean tree) {
		collection.add(this);
		children.forEach(tech -> {
			if (!tree || !tech.isRoot())
				tech.getChildren(collection, tree);
		});
	}

	@Override
	public IResearchRecipe getResearchRecipe() {
		return research;
	}

	@Override
	public boolean hasResearchRecipe() {
		return research != null;
	}

	@Override
	public IIdeaRecipe getIdeaRecipe() {
		return idea;
	}

	@Override
	public boolean hasIdeaRecipe() {
		return idea != null;
	}

	@Override
	public boolean isRoot() {
		return !hasParent()
				|| !getRegistryName().getPath().substring(0, getRegistryName().getPath().indexOf('/')).equals(parent
						.getRegistryName().getPath().substring(0, parent.getRegistryName().getPath().indexOf('/')));
	}

	@Override
	public DisplayInfo getDisplayInfo() {
		return display;
	}

	@Override
	public Technology getParent() {
		return parent;
	}

	@Override
	public boolean hasParent() {
		return parent != null;
	}

	@Override
	public NonNullList<IUnlock> getUnlock() {
		return unlock;
	}

	@Override
	public boolean hasCustomUnlock() {
		return requirements.length > 0;
	}

	@Override
	public void setResearched(Player player, boolean announce) {
		CapabilityTechnology.ITechnology cap = player.getAttachedOrCreate(CapabilityTechnology.TECH_CAP);
		cap.setResearched(getRegistryName().toString());

		if (player instanceof ServerPlayer) {
			ServerPlayer playerMP = (ServerPlayer) player;
			addRecipes(playerMP);

			if (rewards != null)
				rewards.grant(playerMP);

			for (Technology child : children)
				if (!child.isResearched(playerMP))
					child.registerListeners(playerMP);

			Content.c_technologyResearched.trigger((ServerPlayer) player, this);
			TechnologyEvent.Research.EVENT.invoker().accept(new TechnologyEvent.Research(player, this));
		}
		if (announce) {
			player.getServer().getPlayerList().broadcastSystemMessage(
					Component.translatable("chat.type.technology", player.getDisplayName(), displayText), false);
			for (Technology child : children)
				if (child.isRoot() && child.isUnlocked(player))
					player.sendSystemMessage(
							Component.translatable("technology.complete.unlock.root", child.displayText));
			player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
					SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}

	public void addRecipes(ServerPlayer player) {
		unlock.forEach(unlock -> unlock.unlock(player));
	}

	@Override
	public void removeResearched(Player player) {
		CapabilityTechnology.ITechnology cap = player.getAttachedOrCreate(CapabilityTechnology.TECH_CAP);
		if (isResearched(player)) {
			cap.removeResearched(getRegistryName().toString());

			if (player instanceof ServerPlayer) {
				ServerPlayer playerMP = (ServerPlayer) player;

				unlock.forEach(unlock -> unlock.lock(playerMP));
				for (Technology child : children)
					child.unregisterListeners(playerMP);

				TechnologyEvent.Revoke.EVENT.invoker().accept(new TechnologyEvent.Revoke(player, this));
			}
		}
		if (hasCustomUnlock()) {
			TechnologyProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);

			for (String criterion : progress.getCompletedCriteria())
				if (progress.revokeCriterion(criterion))
					cap.removeResearched(getRegistryName() + "#" + criterion);

			if (player instanceof ServerPlayer)
				registerListeners((ServerPlayer) player);
		}
	}

	@Override
	public Map<String, Criterion<?>> getCriteria() {
		return criteria;
	}

	@Override
	public String[][] getRequirements() {
		return requirements;
	}

	@Override
	public boolean grantCriterion(Player player, String name) {
		TechnologyProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);
		boolean done = progress.isDone();

		if (progress.grantCriterion(name)) {
			player.getAttachedOrCreate(CapabilityTechnology.TECH_CAP).setResearched(getRegistryName() + "#" + name);
			if (player instanceof ServerPlayer) {
				ServerPlayer playerMP = (ServerPlayer) player;

				unregisterListeners(playerMP);
				if (!done && progress.isDone() && unlockedStage(player))
					unlock(playerMP);
			}

			return true;
		}

		return false;
	}

	public void unlock(ServerPlayer player) {
		TechnologyEvent.Unlock.EVENT.invoker().accept(new TechnologyEvent.Unlock(player, this));

		player.sendSystemMessage(Component.translatable(
				isRoot() ? "technology.complete.unlock.root" : "technology.complete.unlock", displayText));
		player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
				1.0F, 1.0F);

		Content.c_technologyUnlocked.trigger(player, this);
	}

	@Override
	public boolean revokeCriterion(Player player, String name) {
		TechnologyProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);
		boolean done = progress.isDone();

		if (progress.revokeCriterion(name)) {
			player.getAttachedOrCreate(CapabilityTechnology.TECH_CAP).removeResearched(getRegistryName() + "#" + name);
			if (player instanceof ServerPlayer) {
				registerListeners((ServerPlayer) player);
				if (done && !progress.isDone())
					TechnologyEvent.Revoke.EVENT.invoker().accept(new TechnologyEvent.Revoke(player, this));
			}

			return true;
		}

		return false;
	}

	public void registerListeners(ServerPlayer player) {
		TechnologyProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);
		if (!progress.isDone()) {
			for (Map.Entry<String, Criterion<?>> entry : criteria.entrySet()) {
				Boolean criterionProgress = progress.getCriterionProgress(entry.getKey());
				if (criterionProgress != null && !criterionProgress) {
					CriterionTriggerInstance instance = entry.getValue().triggerInstance();
					if (instance != null) {
						CriterionTrigger<?> trigger = entry.getValue().trigger();
							if (trigger instanceof TriggerFTGU)
								((TriggerFTGU) trigger).addTechListener(player.getAdvancements(), instance,
									new ListenerTechnology(this, entry.getKey()));
							else {
								TechnologyManager.INSTANCE.trackCriterion(player, this, entry.getKey(), instance, trigger);
							}
					}
				}
			}
		}
	}

	public void unregisterListeners(ServerPlayer player) {
		boolean parent = this.parent != null && !this.parent.isResearched(player);
		TechnologyProgress progress = TechnologyManager.INSTANCE.getProgress(player, this);

		for (Map.Entry<String, Criterion<?>> entry : criteria.entrySet()) {
			Boolean criterionProgress = progress.getCriterionProgress(entry.getKey());
			if (criterionProgress != null && (parent || criterionProgress || progress.isDone())) {
				CriterionTriggerInstance instance = entry.getValue().triggerInstance();
				if (instance != null) {
					CriterionTrigger<?> trigger = entry.getValue().trigger();
						if (trigger instanceof TriggerFTGU)
						((TriggerFTGU) trigger).removeTechListener(player.getAdvancements(), instance,
								new ListenerTechnology(this, entry.getKey()));
						else
							TechnologyManager.INSTANCE.untrackCriterion(player, this, entry.getKey());
				}
			}
		}
	}

	@Override
	public Component getDisplayText() {
		return displayText;
	}

	public boolean hasProgress(Player player) {
		return isResearched(player)
				|| (hasCustomUnlock() && TechnologyManager.INSTANCE.getProgress(player, this).hasProgress());
	}

	@Override
	public boolean isResearched(Player player) {
		CapabilityTechnology.ITechnology cap = player.getAttachedOrCreate(CapabilityTechnology.TECH_CAP);
		return cap.isResearched(getRegistryName().toString());
	}

	public boolean isUnlockedIgnoreStage(Player player) {
		return !hasCustomUnlock() || TechnologyManager.INSTANCE.getProgress(player, this).isDone();
	}

	@Override
	public boolean isUnlocked(Player player) {
		return unlockedStage(player) && isUnlockedIgnoreStage(player);
	}

	@Override
	public boolean canResearch(Player player) {
		return !isResearched(player) && isUnlocked(player) && (parent == null || parent.isResearched(player));
	}

	@Override
	public ITechnologyBuilder toBuilder() {
		return new TechnologyBuilder(this);
	}

	@Override
	public String getGameStage() {
		return stage;
	}

	private boolean unlockedStage(Player player) {
		return stage == null;
	}

	public boolean canResearchIgnoreCustomUnlock(Player player) {
		return !isResearched(player) && (parent == null || parent.isResearched(player));
	}

	public boolean canResearchIgnoreResearched(Player player) {
		return isResearched(player) || isUnlocked(player) && (parent == null || parent.isResearched(player));
	}

	@Override
	public ResourceLocation getRegistryName() {
		return id;
	}

	public static class Builder {

		private final ResourceLocation parentId;
		private final DisplayInfo display;
		private final AdvancementRewards rewards;
		private final Map<String, Criterion<?>> criteria;
		private final String[][] requirements;

		private final JsonArray unlock;
		private final JsonObject idea;
		private final JsonObject research;

		private final String stage;

		private final boolean start;
		private final boolean copy;

		private Technology parent;

		private Builder(@Nullable ResourceLocation parent, DisplayInfo display, AdvancementRewards rewards,
				Map<String, Criterion<?>> criteria, String[][] requirements, boolean start, boolean copy,
				@Nullable JsonArray unlock, @Nullable JsonObject idea, @Nullable JsonObject research, String stage) {
			this.parentId = parent;
			this.display = display;
			this.rewards = rewards;
			this.criteria = criteria;
			this.requirements = requirements;
			this.start = start;
			this.copy = copy;
			this.unlock = unlock;
			this.idea = idea;
			this.research = research;
			this.stage = stage;
		}

		public boolean resolveParent(Map<ResourceLocation, Technology> map) {
			if (parentId == null)
				return true;
			parent = map.get(parentId);
			return parent != null;
		}

		public Technology build(ResourceLocation location, JsonContextPublic context) {
			NonNullList<IUnlock> unlock = NonNullList.create();
			if (this.unlock != null)
				for (JsonElement element : this.unlock)
					unlock.add(TechnologyManager.INSTANCE.getUnlock(element, context, location));

			IIdeaRecipe idea = this.idea == null ? null : IdeaRecipe.deserialize(this.idea, context);
			IResearchRecipe research = this.research == null ? null
					: TechnologyManager.INSTANCE.getPuzzle(this.research, context, location);

			Technology r = new Technology(location, parent, display, rewards, criteria, requirements, start, copy,
					unlock, idea, research, stage);
			if (research != null)
				research.setTechnology(r);
			return r;
		}

	}

	public static class Deserializer implements JsonDeserializer<Builder> {

		@Override
		public Builder deserialize(JsonElement element, java.lang.reflect.Type ignore,
				JsonDeserializationContext context) throws JsonParseException {
			if (!element.isJsonObject())
				throw new JsonSyntaxException("Expected technology to be an object");
			JsonObject json = element.getAsJsonObject();

			ResourceLocation parent = json.has("parent") ? ResourceLocation.parse(GsonHelper.getAsString(json, "parent"))
					: null;

			JsonObject displayObject = GsonHelper.getAsJsonObject(json, "display");
			if (!displayObject.has("id"))
				displayObject.addProperty("id", parent != null ? parent.toString() : "ftgumod:root");

			// Convert 1.12.2 icon format {"item": "..."} to 1.21.1 {"id": "..."}
			if (displayObject.has("icon")) {
				JsonObject icon = displayObject.getAsJsonObject("icon");
				if (icon.has("item") && !icon.has("id")) {
					String itemName = GsonHelper.getAsString(icon, "item");
					int data = icon.has("data") ? GsonHelper.getAsInt(icon, "data") : 0;
					icon.addProperty("id", StackUtils.INSTANCE.remapItem(itemName, data));
					icon.remove("item");
				}
				icon.remove("data");
			}

			DisplayInfo display = DisplayInfo.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, displayObject).getOrThrow(JsonSyntaxException::new);

			if (displayObject.has("x") || displayObject.has("y"))
				display.setLocation(GsonHelper.getAsFloat(displayObject, "x"), GsonHelper.getAsFloat(displayObject, "y"));

			AdvancementRewards rewards = AdvancementRewards.EMPTY;
				if (json.has("rewards"))
					rewards = AdvancementRewards.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, json.get("rewards"))
							.getOrThrow(JsonSyntaxException::new);
			Map<String, Criterion<?>> criteria;
				if (json.has("criteria")) {
					criteria = new java.util.HashMap<>();
					JsonObject criteriaJson = GsonHelper.getAsJsonObject(json, "criteria");
					for (Map.Entry<String, JsonElement> entry : criteriaJson.entrySet()) {
						try {
							net.minecraft.resources.RegistryOps<JsonElement> ops = net.minecraft.resources.RegistryOps
									.create(com.mojang.serialization.JsonOps.INSTANCE, TechnologyManager.INSTANCE.getRegistryAccess());
							Criterion<?> c = Criterion.CODEC.parse(ops, entry.getValue()).getOrThrow(JsonSyntaxException::new);
							criteria.put(entry.getKey(), c);
						} catch (JsonSyntaxException e) {
							if (e.getMessage() == null || !e.getMessage().contains("Can't access registry"))
								LOGGER.warn("Skipping unparseable criterion '{}': {}", entry.getKey(), e.getMessage());
						}
					}
				} else {
					criteria = Collections.emptyMap();
				}

			JsonArray array = GsonHelper.getAsJsonArray(json, "requirements", new JsonArray());
			java.util.List<String[]> reqList = new java.util.ArrayList<>();

			for (int i = 0; i < array.size(); ++i) {
				JsonArray subarray = GsonHelper.convertToJsonArray(array.get(i), "requirements[" + i + "]");
				java.util.List<String> filtered = new java.util.ArrayList<>();

				for (int j = 0; j < subarray.size(); ++j) {
					String name = GsonHelper.convertToString(subarray.get(j), "requirements[" + i + "][" + j + "]");
					if (criteria.containsKey(name))
						filtered.add(name);
					
						// silently skip — criterion may be deferred from client loading
				}
				if (!filtered.isEmpty())
					reqList.add(filtered.toArray(new String[0]));
			}

			String[][] requirements;
			if (reqList.isEmpty() && !criteria.isEmpty()) {
				requirements = new String[criteria.size()][];
				int k = 0;
				for (String s2 : criteria.keySet())
					requirements[k++] = new String[] { s2 };
			} else {
				requirements = reqList.toArray(new String[0][]);
			}

			if (requirements.length == 0 && !criteria.isEmpty())
				requirements = new String[][] { criteria.keySet().toArray(new String[0]) };

			for (String s1 : criteria.keySet()) {
				boolean flag = false;

				for (String[] subarray : requirements) {
					if (ArrayUtils.contains(subarray, s1)) {
						flag = true;
						break;
					}
				}

				if (!flag)
					LOGGER.warn("Criterion '{}' isn't a requirement — auto-adding to requirements", s1);
			}

			JsonArray unlock = json.has("unlock") ? GsonHelper.getAsJsonArray(json, "unlock") : null;
			JsonObject idea = json.has("idea") ? GsonHelper.getAsJsonObject(json, "idea") : null;
			JsonObject research = json.has("research") ? GsonHelper.getAsJsonObject(json, "research") : null;

			String stage = GsonHelper.getAsString(json, "gamestage", null);

			boolean start = GsonHelper.getAsBoolean(json, "start", false);
			boolean copy = GsonHelper.getAsBoolean(json, "copy", true);

			return new Builder(parent, display, rewards, criteria, requirements, start, copy, unlock, idea, research,
					stage);
		}

	}

}
