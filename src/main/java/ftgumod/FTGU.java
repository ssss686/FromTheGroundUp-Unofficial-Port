package ftgumod;

import java.io.File;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ftgumod.api.technology.puzzle.ResearchConnect;
import ftgumod.api.technology.puzzle.ResearchMatch;
import ftgumod.api.util.predicate.ItemFluid;
import ftgumod.api.util.predicate.ItemLambda;
import ftgumod.api.util.predicate.ItemMod;
import ftgumod.command.CommandTechnology;
import ftgumod.packet.PacketDispatcher;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.util.StackUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;

@Mod(FTGU.MODID)
public class FTGU {

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Technology.Builder.class, new Technology.Deserializer())
			.create();

	public static final String MODID = "ftgumod";

	public static boolean JEI_LOADED = false;

	public static File configFolder;

	public FTGU(IEventBus modEventBus, ModContainer modContainer) {
		Content.BLOCKS.register(modEventBus);
		Content.ITEMS.register(modEventBus);
		Content.BLOCK_ENTITY_TYPES.register(modEventBus);
		Content.MENU_TYPES.register(modEventBus);
		Content.CREATIVE_MODE_TABS.register(modEventBus);
		Content.TRIGGER_TYPES.register(modEventBus);

		StackUtils.INSTANCE.registerItemPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "fluid"), new ItemFluid.Factory());
		StackUtils.INSTANCE.registerItemPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "enchantment"),
				new ItemLambda.Factory(i -> !net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(i).isEmpty()));
		StackUtils.INSTANCE.registerItemPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "mod"), new ItemMod.Factory());

		TechnologyManager.INSTANCE.registerPuzzle(ResourceLocation.fromNamespaceAndPath(MODID, "match"), new ResearchMatch.Factory());
		TechnologyManager.INSTANCE.registerPuzzle(ResourceLocation.fromNamespaceAndPath(MODID, "connect"), new ResearchConnect.Factory());

		CapabilityTechnology.ATTACHMENT_TYPES.register(modEventBus);
		NeoForge.EVENT_BUS.register(new CapabilityTechnology());
		NeoForge.EVENT_BUS.register(new EventHandler());

		PacketDispatcher.registerPackets(modEventBus);

		configFolder = FMLPaths.CONFIGDIR.get().resolve(MODID).toFile();

		modContainer.registerConfig(ModConfig.Type.COMMON, FTGUConfig.SPEC);

		JEI_LOADED = ModList.get().isLoaded("jei");

		modEventBus.addListener(this::loadComplete);

		NeoForge.EVENT_BUS.addListener(this::serverStarted);
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
	}

	private void loadComplete(net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent event) {
		TechnologyManager.INSTANCE.loadClient();
	}

	private void serverStarted(ServerStartedEvent event) {
		TechnologyManager.INSTANCE.setRegistryAccess(event.getServer().registryAccess());
		TechnologyManager.INSTANCE.reload(event.getServer().getWorldPath(
				net.minecraft.world.level.storage.LevelResource.ROOT).toFile());
	}

	private void registerCommands(RegisterCommandsEvent event) {
		CommandTechnology.register(event.getDispatcher());
	}

}
