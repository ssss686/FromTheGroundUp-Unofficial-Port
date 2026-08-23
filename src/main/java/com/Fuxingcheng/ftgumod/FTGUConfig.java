package com.Fuxingcheng.ftgumod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = FTGU.MODID)
public final class FTGUConfig {

	public static final ForgeConfigSpec SPEC;

	public static final ForgeConfigSpec.BooleanValue allowResearchCopy;
	public static final ForgeConfigSpec.BooleanValue loadDefaultTechnologies;
	public static final ForgeConfigSpec.BooleanValue giveResearchBook;
	public static final ForgeConfigSpec.EnumValue<HideJeiItems> jeiHide;

	public static boolean cachedLoadDefaultTechnologies;
	public static boolean cachedGiveResearchBook;
	public static boolean cachedAllowResearchCopy;
	public static HideJeiItems cachedJeiHide;

	public enum HideJeiItems {
		NOTHING,
		LOCKED_RECIPES,
		LOCKED_RECIPES_AND_ITEMS
	}

	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		builder.comment("FTGU Mod Configuration").push("ftgumod");

		allowResearchCopy = builder
				.comment("If enabled, researches can be copied")
				.define("allowResearchCopy", true);

		loadDefaultTechnologies = builder
				.comment("If disabled, default technologies will not be loaded")
				.define("loadDefaultTechnologies", true);

		giveResearchBook = builder
				.comment("If enabled, every player will get a research book when they join a new world or server")
				.define("giveResearchBook", true);

		jeiHide = builder
				.comment("Jei hide mode. You can hide nothing, locked recipes or locked recipes and items in JEI")
				.defineEnum("jeiHide", HideJeiItems.LOCKED_RECIPES);

		builder.pop();

		SPEC = builder.build();
		cachedLoadDefaultTechnologies = true;
		cachedGiveResearchBook = true;
		cachedAllowResearchCopy = true;
		cachedJeiHide = HideJeiItems.LOCKED_RECIPES;
	}

	@SubscribeEvent
	public static void onLoad(ModConfigEvent event) {
		if (event.getConfig().getModId().equals(FTGU.MODID)) {
			cachedLoadDefaultTechnologies = loadDefaultTechnologies.get();
			cachedGiveResearchBook = giveResearchBook.get();
			cachedAllowResearchCopy = allowResearchCopy.get();
			cachedJeiHide = jeiHide.get();
		}
	}
}