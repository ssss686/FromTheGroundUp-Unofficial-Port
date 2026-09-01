package com.Fuxingcheng.ftgumod.technology;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.Fuxingcheng.ftgumod.FTGU;
import com.Fuxingcheng.ftgumod.FTGUConfig;
import com.Fuxingcheng.ftgumod.api.FTGUAPI;
import com.Fuxingcheng.ftgumod.api.technology.ITechnology;
import com.Fuxingcheng.ftgumod.api.technology.ITechnologyManager;
import com.Fuxingcheng.ftgumod.api.technology.recipe.IResearchRecipe;
import com.Fuxingcheng.ftgumod.api.technology.unlock.IUnlock;
import com.Fuxingcheng.ftgumod.api.technology.unlock.UnlockCompound;
import com.Fuxingcheng.ftgumod.api.technology.unlock.UnlockRecipe;
import com.Fuxingcheng.ftgumod.api.util.JsonContextPublic;
import com.Fuxingcheng.ftgumod.packet.PacketDispatcher;
import com.Fuxingcheng.ftgumod.packet.client.TechnologyMessage;
import com.Fuxingcheng.ftgumod.util.StackUtils;
import com.Fuxingcheng.ftgumod.util.SubCollection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.fml.ModContainer;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.advancements.AdvancementHolder;

public class TechnologyManager implements ITechnologyManager, Iterable<Technology> {

	public static final TechnologyManager INSTANCE = new TechnologyManager();

	public static CommandSourceStack player;

	static {
		FTGUAPI.technologyManager = INSTANCE;
	}

	private final Map<UUID, Map<Technology, TechnologyProgress>> progress = new HashMap<>();

	private final Map<ResourceLocation, Technology> technologies = new LinkedHashMap<>();
	private final Collection<Technology> roots = new SubCollection<>(technologies.values(), Technology::isRoot);
	private final Collection<Technology> start = new SubCollection<>(technologies.values(),
			Technology::researchedAtStart);

	private final Map<ResourceLocation, IUnlock.Factory<?>> unlocks = new HashMap<>();
	private final Map<ResourceLocation, IResearchRecipe.Factory<?>> puzzles = new HashMap<>();

	private final List<Predicate<? super ITechnology>> removeCallback = new LinkedList<>();
	private final List<Consumer<? super ITechnology>> addCallback = new LinkedList<>();
	private final List<Runnable> createCallback = new LinkedList<>();

	public Map<String, Pair<String, Map<ResourceLocation, String>>> cache;

	private net.minecraft.core.RegistryAccess registryAccess = net.minecraft.core.RegistryAccess.EMPTY;

	public net.minecraft.core.RegistryAccess getRegistryAccess() {
		return registryAccess;
	}

	public void setRegistryAccess(net.minecraft.core.RegistryAccess registryAccess) {
		this.registryAccess = registryAccess;
	}

	private final Map<ServerPlayer, List<PendingCriterion>> pendingCriteria = new HashMap<>();

	private final Map<ServerPlayer, Map<AdvancementHolder, Pair<Technology, String>>> fakeAdvancements = new HashMap<>();

	public record PendingCriterion(Technology tech, String criterionName, CriterionTriggerInstance instance, CriterionTrigger<?> trigger) {}

	private Map<JsonContextPublic, Map<ResourceLocation, String>> loadBuiltin() {
		Map<JsonContextPublic, Map<ResourceLocation, String>> json = new HashMap<>();

		ModList.get().getMods().forEach(mod -> {
			JsonContextPublic context = new JsonContextPublic(mod.getModId());

			Map<ResourceLocation, String> map = new HashMap<>();

			IModFileInfo modFile = mod.getOwningFile();
			if (modFile == null) return;

			Path basePath = modFile.getFile().findResource("assets/" + mod.getModId() + "/technologies");
			if (basePath == null || !Files.exists(basePath)) return;

			// Walk all JSON files
			try {
				Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						String relative = basePath.relativize(file).toString().replace('\\', '/');
						if (!relative.endsWith(".json") || relative.startsWith("_") || !relative.contains("/"))
							return FileVisitResult.CONTINUE;

						String name = FilenameUtils.removeExtension(relative);
						ResourceLocation id = ResourceLocation.fromNamespaceAndPath(mod.getModId(), name);

						try {
							map.put(id, new String(Files.readAllBytes(file)));
						} catch (IOException | JsonParseException e) {
							error("Couldn't read technology {} from {}", id, file, e);
						}

						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				error("Couldn't read technologies from {}", mod.getModId(), e);
			}

			json.put(context, map);
		});
		return json;
	}

	public void unloadProgress(Player player) {
		progress.remove(player.getUUID());
	}

	public IUnlock getUnlock(JsonElement element, JsonContextPublic context, ResourceLocation tech) {
		if (element.isJsonArray()) {
			NonNullList<IUnlock> unlocks = NonNullList.create();
			element.getAsJsonArray().forEach(json -> unlocks.add(getUnlock(json, context, tech)));
			return new UnlockCompound(unlocks);
		} else if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("type")) {
				ResourceLocation type = ResourceLocation.parse(context.appendModId(GsonHelper.getAsString(object, "type")));
				if (unlocks.containsKey(type))
					return unlocks.get(type).deserialize(object, context, tech);
			}
		}
		return new UnlockRecipe(StackUtils.INSTANCE.getItemPredicate(element, context).getIngredient());
	}

	public IResearchRecipe getPuzzle(JsonElement element, JsonContextPublic context, ResourceLocation technology) {
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("type")) {
				ResourceLocation type = ResourceLocation.parse(context.appendModId(GsonHelper.getAsString(object, "type")));
				if (puzzles.containsKey(type))
					return puzzles.get(type).deserialize(object, context, technology);
				throw new JsonSyntaxException("Unknown puzzle type " + type);
			} else
				throw new JsonSyntaxException("IPuzzle missing required field 'type'");
		} else
			throw new JsonSyntaxException("Expected puzzle to be an object");
	}

	public Collection<Technology> getRoots() {
		return roots;
	}

	public Collection<Technology> getStart() {
		return start;
	}

	@Override
	public boolean isLocked(ItemStack stack, @Nullable Player player) {
		boolean tech = false;
		if (!stack.isEmpty()) {
			for (Technology t : technologies.values()) {
				for (IUnlock unlock : t.getUnlock()) {
					if (unlock.unlocks(stack)) {
						if (player == null)
							return true;
						if (t.isResearched(player))
							return false;
						tech = true;
						break;
					}
				}
			}
		}
		return tech;
	}

	@Override
	public void removeCallback(Predicate<? super ITechnology> predicate) {
		removeCallback.add(predicate);
	}

	@Override
	public void addCallback(Consumer<? super ITechnology> action) {
		addCallback.add(action);
	}

	@Override
	public void createCallback(Runnable action) {
		createCallback.add(action);
	}

	public TechnologyProgress getProgress(Player player, Technology technology) {
		return progress.computeIfAbsent(player.getUUID(), uuid -> new HashMap<>()).computeIfAbsent(technology,
				tech -> {
					TechnologyProgress progress = new TechnologyProgress();

					progress.update(tech.getCriteria(), tech.getRequirements());

					CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP)
							.orElse(new CapabilityTechnology.DefaultImpl());
					for (String criterion : progress.getRemaningCriteria())
						if (cap.isResearched(tech.getRegistryName().toString() + "#" + criterion))
							progress.grantCriterion(criterion);

					return progress;
				});
	}

	public void clear() {
		progress.clear();
		technologies.clear();

		createCallback.forEach(Runnable::run);
	}

	/** Load technologies from built-in mod resources on the client side. */
	public void loadClient() {
		clear();
		cache = new HashMap<>();
		load();
	}

	public void reload(File data) {
		clear();

		cache = new HashMap<>();
		load(new File(FTGU.configFolder, "technologies"));
		load(new File(data, "technologies"));

		load();
	}

	private void load(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			for (File child : dir.listFiles(File::isDirectory)) {
				Map<ResourceLocation, String> techs = new HashMap<>();
				for (File file : FileUtils.listFiles(child, new String[] { "json" }, true)) {
					if (file.getParentFile().equals(child))
						continue;
					ResourceLocation id = ResourceLocation.fromNamespaceAndPath(child.getName(), FilenameUtils
							.removeExtension(child.toPath().relativize(file.toPath()).toString().replace('\\', '/')));

					try {
						techs.put(id, new String(Files.readAllBytes(file.toPath())));
					} catch (IOException e) {
						error("Couldn't read technology {} from {}", id, file, e);
					}
				}

				if (cache.containsKey(child.getName())) {
					cache.get(child.getName()).getRight().forEach(techs::putIfAbsent);
				}
				cache.put(child.getName(), Pair.of("[]", techs));
			}
		} else
			dir.mkdirs();
	}

	public void removeFromCache(ResourceLocation tech) {
		if (cache.containsKey(tech.getNamespace())) {
			Map<ResourceLocation, String> map = cache.get(tech.getNamespace()).getRight();
			map.remove(tech);
			if (map.isEmpty())
				cache.remove(tech.getNamespace());
		}
	}

	public void load() {
		Map<JsonContextPublic, Map<ResourceLocation, String>> json = cache.entrySet().stream()
				.collect(Collectors.toMap(entry -> new JsonContextPublic(entry.getKey()),
						entry -> entry.getValue().getRight()));

		if (FTGUConfig.cachedLoadDefaultTechnologies) {
			loadBuiltin().forEach((context, map) -> {
				if (!json.containsKey(context))
					json.put(context, map);
				else
					map.forEach(json.get(context)::putIfAbsent);
			});
		}

		Map<JsonContextPublic, Map<ResourceLocation, Technology.Builder>> builders = new HashMap<>();
		Map<ResourceLocation, Technology> technologies = new LinkedHashMap<>();

		for (Map.Entry<JsonContextPublic, Map<ResourceLocation, String>> domain : json.entrySet()) {
			Map<ResourceLocation, Technology.Builder> map = new HashMap<>();
			for (Map.Entry<ResourceLocation, String> file : domain.getValue().entrySet()) {
				try {
					map.put(file.getKey(), FTGU.GSON.fromJson(file.getValue(), Technology.Builder.class));
				} catch (JsonParseException e) {
					removeFromCache(file.getKey());
					error("Couldn't load technology " + file.getKey(), e);
				}
			}
			builders.put(domain.getKey(), map);
		}

		boolean load = true;
		while (!builders.isEmpty() && load) {
			load = false;

			for (Map.Entry<JsonContextPublic, Map<ResourceLocation, Technology.Builder>> domain : builders.entrySet()) {
				Iterator<Map.Entry<ResourceLocation, Technology.Builder>> iterator = domain.getValue().entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Map.Entry<ResourceLocation, Technology.Builder> entry = iterator.next();

					if (entry.getValue().resolveParent(technologies)) {
						try {
							Technology technology = entry.getValue().build(entry.getKey(), domain.getKey());
							technologies.put(technology.getRegistryName(), technology);
							load = true;
						} catch (JsonParseException e) {
							removeFromCache(entry.getKey());
							error("Couldn't load technology " + entry.getKey(), e);
						}

						iterator.remove();
					}
				}
			}

			if (!load) {
				for (Map.Entry<JsonContextPublic, Map<ResourceLocation, Technology.Builder>> domain : builders
						.entrySet()) {
					for (Map.Entry<ResourceLocation, Technology.Builder> entry : domain.getValue().entrySet()) {
						removeFromCache(entry.getKey());
						error("Couldn't load technology " + entry.getKey(),
								"Parent couldn't be loaded or doesn't exist");
					}
				}
			}
		}

		registerAll(technologies.values().toArray(new Technology[technologies.size()]));

		int size = this.technologies.size();
		info("Loaded " + size + " technolog" + (size != 1 ? "ies" : "y"));
	}

	private static void printToPlayer(String string) {
		if (player != null) {
			player.sendSystemMessage(Component.literal(string)
					.withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)));
		}
	}

	private static void error(String string, Object p1, Object p2, Exception e) {
		Technology.getLogger().error(string, p1, p2, e);
		printToPlayer(String.format(string, p1, p2) + "\n " + e.getClass().getSimpleName() + ": " + e.getMessage());
	}

	private static void error(String string, Object p1, Exception e) {
		Technology.getLogger().error(string, p1, e);
		printToPlayer(String.format(string, p1) + "\n " + e.getClass().getSimpleName() + ": " + e.getMessage());
	}

	private static void error(String string, Exception e) {
		Technology.getLogger().error(string, e);
		printToPlayer(string + "\n " + e.getClass().getSimpleName() + ": " + e.getMessage());
	}

	private static void error(String string, String e) {
		Technology.getLogger().error(string);
		printToPlayer(string + "\n " + e);
	}

	private static void info(String string) {
		Technology.getLogger().info(string);
		printToPlayer(string);
	}

	@Override
	public void register(ITechnology value) {
		if (value instanceof Technology) {
			if (_register((Technology) value))
				addCallback.forEach(action -> action.accept(value));
		} else
			throw new IllegalArgumentException("Technology instance is of unexpected class");
	}

	private boolean _register(Technology value) {
		if (value == null || value.getRegistryName() == null)
			throw new NullPointerException("Tried to register a technology that is null or has a null registry name");

		for (Predicate<? super ITechnology> predicate : removeCallback)
			if (predicate.test(value))
				return false;

		if (value.hasParent())
			value.getParent().getChildren().add(value);

		technologies.put(value.getRegistryName(), value);
		if (value.start)
			autoResearch(value);
		return true;
	}

	@Override
	public void registerAll(ITechnology... values) {
		for (ITechnology tech : values)
			register(tech);
	}

	@Override
	public boolean contains(ResourceLocation key) {
		return technologies.containsKey(key);
	}

	@Override
	public boolean contains(ITechnology value) {
		return technologies.containsValue(value);
	}

	@Nullable
	@Override
	public Technology getTechnology(ResourceLocation key) {
		return technologies.get(key);
	}

	@Override
	public TechnologyBuilder createBuilder(ResourceLocation id) {
		return new TechnologyBuilder(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<ITechnology> getTechnologies() {
		return (Collection) technologies.values();
	}

	@Override
	public Set<ResourceLocation> getRegistryNames() {
		return technologies.keySet();
	}

	@Override
	public void sync(ServerPlayer player, ITechnology... toasts) {
		PacketDispatcher.sendTo(new TechnologyMessage(player, false, toasts), player);
	}

	@Override
	public void addRecipes(Collection<RecipeHolder<?>> recipes, ServerPlayer player) {
		List<RecipeHolder<?>> filtered = new LinkedList<>(recipes);
		filtered.removeIf(holder -> isLocked(
				holder.value().getResultItem(player.serverLevel().registryAccess()), player));
		if (!filtered.isEmpty())
			player.awardRecipes(filtered);
	}

	@Override
	public void registerUnlock(ResourceLocation name, IUnlock.Factory<?> factory) {
		unlocks.put(name, factory);
	}

	@Override
	public void registerPuzzle(ResourceLocation name, IResearchRecipe.Factory<?> factory) {
		puzzles.put(name, factory);
	}

	@Override
	public Iterator<Technology> iterator() {
		return technologies.values().iterator();
	}


	public void trackCriterion(ServerPlayer player, Technology tech, String name, CriterionTriggerInstance instance, CriterionTrigger<?> trigger) {
		pendingCriteria.computeIfAbsent(player, k -> new ArrayList<>()).add(new PendingCriterion(tech, name, instance, trigger));
	}

	public void untrackCriterion(ServerPlayer player, Technology tech, String name) {
		List<PendingCriterion> list = pendingCriteria.get(player);
		if (list != null) {
			list.removeIf(pc -> pc.tech.equals(tech) && pc.criterionName.equals(name));
			if (list.isEmpty())
				pendingCriteria.remove(player);
		}
	}

	public void trackFakeAdvancement(ServerPlayer player, AdvancementHolder holder, Technology tech, String criterionName) {
		fakeAdvancements.computeIfAbsent(player, k -> new HashMap<>()).put(holder, Pair.of(tech, criterionName));
	}

	public void untrackFakeAdvancement(ServerPlayer player, AdvancementHolder holder) {
		Map<AdvancementHolder, Pair<Technology, String>> map = fakeAdvancements.get(player);
		if (map != null) {
			map.remove(holder);
			if (map.isEmpty())
				fakeAdvancements.remove(player);
		}
	}

	public Map<ServerPlayer, Map<AdvancementHolder, Pair<Technology, String>>> getFakeAdvancements() {
		return fakeAdvancements;
	}

	public Map<ServerPlayer, List<PendingCriterion>> getPendingCriteria() {
		return pendingCriteria;
	}

	public static void autoResearch(Technology tech) {
		MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
		if (server != null)
			server.getPlayerList().getPlayers().forEach(player -> {
				CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP)
						.orElse(new CapabilityTechnology.DefaultImpl());
				cap.setResearched(tech.getRegistryName().toString());
			});
	}

	public enum GUI {
		IDEATABLE, RESEARCHTABLE
	}

}