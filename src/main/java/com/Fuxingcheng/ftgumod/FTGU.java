package com.Fuxingcheng.ftgumod;

import java.io.File;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.Fuxingcheng.ftgumod.api.technology.puzzle.ResearchConnect;
import com.Fuxingcheng.ftgumod.api.technology.puzzle.ResearchMatch;
import com.Fuxingcheng.ftgumod.api.util.predicate.ItemFluid;
import com.Fuxingcheng.ftgumod.api.util.predicate.ItemLambda;
import com.Fuxingcheng.ftgumod.api.util.predicate.ItemMod;
import com.Fuxingcheng.ftgumod.command.CommandTechnology;
import com.Fuxingcheng.ftgumod.packet.PacketDispatcher;
import com.Fuxingcheng.ftgumod.technology.CapabilityTechnology;
import com.Fuxingcheng.ftgumod.technology.Technology;
import com.Fuxingcheng.ftgumod.technology.TechnologyManager;
import com.Fuxingcheng.ftgumod.util.StackUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(FTGU.MODID)
public class FTGU {

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Technology.Builder.class, new Technology.Deserializer())
			.create();

	public static final String MODID = "ftgumod";

	public static boolean JEI_LOADED = false;

	public static File configFolder;

	@SuppressWarnings("removal")
	public FTGU() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// 注册内容
		Content.BLOCKS.register(modEventBus);
		Content.ITEMS.register(modEventBus);
		Content.BLOCK_ENTITY_TYPES.register(modEventBus);
		Content.MENU_TYPES.register(modEventBus);
		Content.CREATIVE_MODE_TABS.register(modEventBus);
		Content.TRIGGER_TYPES.register(modEventBus);

		// 注册 Item 谓词
		StackUtils.INSTANCE.registerItemPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "fluid"), new ItemFluid.Factory());
		StackUtils.INSTANCE.registerItemPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "enchantment"),
				new ItemLambda.Factory(i -> !net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(i).isEmpty()));
		StackUtils.INSTANCE.registerItemPredicate(ResourceLocation.fromNamespaceAndPath(MODID, "mod"), new ItemMod.Factory());

		// 注册拼图
		TechnologyManager.INSTANCE.registerPuzzle(ResourceLocation.fromNamespaceAndPath(MODID, "match"), new ResearchMatch.Factory());
		TechnologyManager.INSTANCE.registerPuzzle(ResourceLocation.fromNamespaceAndPath(MODID, "connect"), new ResearchConnect.Factory());

		// 注册事件监听
		MinecraftForge.EVENT_BUS.register(new CapabilityTechnology());
		MinecraftForge.EVENT_BUS.register(new EventHandler());

		// 网络包
		PacketDispatcher.registerPackets();

		// 配置文件目录
		configFolder = FMLPaths.CONFIGDIR.get().resolve(MODID).toFile();

		// 注册配置
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FTGUConfig.SPEC);

		// JEI 检测
		JEI_LOADED = net.minecraftforge.fml.ModList.get().isLoaded("jei");

		// 其他事件监听
		modEventBus.addListener(this::loadComplete);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
	}

	private void loadComplete(FMLLoadCompleteEvent event) {
		TechnologyManager.INSTANCE.loadClient();
	}

	private void serverStarted(ServerStartedEvent event) {
		TechnologyManager.INSTANCE.setRegistryAccess(event.getServer().registryAccess());
		TechnologyManager.INSTANCE.reload(event.getServer().getWorldPath(LevelResource.ROOT).toFile());
	}

	private void registerCommands(RegisterCommandsEvent event) {
		CommandTechnology.register(event.getDispatcher());
	}
}