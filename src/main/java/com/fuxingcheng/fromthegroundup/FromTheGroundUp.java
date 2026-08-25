package com.fuxingcheng.fromthegroundup;

import java.io.File;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fuxingcheng.fromthegroundup.api.technology.puzzle.ResearchConnect;
import com.fuxingcheng.fromthegroundup.api.technology.puzzle.ResearchMatch;
import com.fuxingcheng.fromthegroundup.api.util.predicate.ItemFluid;
import com.fuxingcheng.fromthegroundup.api.util.predicate.ItemLambda;
import com.fuxingcheng.fromthegroundup.api.util.predicate.ItemMod;
import com.fuxingcheng.fromthegroundup.command.CommandTechnology;
import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.technology.CapabilityTechnology;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import com.fuxingcheng.fromthegroundup.util.StackUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FromTheGroundUp implements ModInitializer {

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Technology.Builder.class, new Technology.Deserializer())
			.create();

	public static final String MOD_ID = "ftgumod";
	public static final String MODID = MOD_ID;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean JEI_LOADED = false;

	public static File configFolder;

	@Override
	public void onInitialize() {
		// Register blocks, items, block entities, menus, creative tabs, triggers
		Content.registerAll();

		// Register item predicates
		StackUtils.INSTANCE.registerItemPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "fluid"), new ItemFluid.Factory());
		StackUtils.INSTANCE.registerItemPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "enchantment"),
				new ItemLambda.Factory(i -> !net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(i).isEmpty()));
		StackUtils.INSTANCE.registerItemPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "mod"), new ItemMod.Factory());

		// Register puzzles
		TechnologyManager.INSTANCE.registerPuzzle(ResourceLocation.fromNamespaceAndPath(MODID, "match"), new ResearchMatch.Factory());
		TechnologyManager.INSTANCE.registerPuzzle(ResourceLocation.fromNamespaceAndPath(MODID, "connect"), new ResearchConnect.Factory());

		// Register capability/attachment
		CapabilityTechnology.register();

		// Register event handler
		EventHandler.register();

		// Register loot table modifications
		com.fuxingcheng.fromthegroundup.loot.LootTableHandler.register();

		// Register packets
		PacketDispatcher.registerPackets();

		// Config folder
		Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MODID);
		configDir.toFile().mkdirs();
		configFolder = configDir.toFile();

		// Load config
		FTGUConfig.load();

		// Check JEI
		JEI_LOADED = FabricLoader.getInstance().isModLoaded("jei");

		// Server lifecycle events
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			com.fuxingcheng.fromthegroundup.util.ServerHelper.setServer(server);
			TechnologyManager.INSTANCE.setRegistryAccess(server.registryAccess());
			TechnologyManager.INSTANCE.reload(server.getWorldPath(
					net.minecraft.world.level.storage.LevelResource.ROOT).toFile());
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			com.fuxingcheng.fromthegroundup.util.ServerHelper.setServer(null);
		});

		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CommandTechnology.register(dispatcher);
		});

		// Load client data
		TechnologyManager.INSTANCE.loadClient();

		LOGGER.info("FromTheGroundUp (Fabric) initialized!");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
