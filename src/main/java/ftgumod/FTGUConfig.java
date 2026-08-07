package ftgumod;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = FTGU.MODID)
public final class FTGUConfig {

	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.BooleanValue allowResearchCopy;
	public static final ModConfigSpec.BooleanValue loadDefaultTechnologies;
	public static final ModConfigSpec.BooleanValue giveResearchBook;
	public static final ModConfigSpec.EnumValue<HideJeiItems> jeiHide;

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
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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
