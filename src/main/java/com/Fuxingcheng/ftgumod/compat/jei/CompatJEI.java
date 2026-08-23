package com.Fuxingcheng.ftgumod.compat.jei;

import com.Fuxingcheng.ftgumod.FTGU;
import com.Fuxingcheng.ftgumod.FTGUConfig;

/**
 * JEI integration stub.
 * Full JEI hiding requires JEI as a compile dependency.
 * When JEI is added, implement {@code IRecipeManagerPlugin} or use JEI's runtime API
 * to hide locked recipes/items based on {@link FTGUConfig.HideJeiItems}.
 */
public class CompatJEI {

	public static void refreshHiddenItems(boolean refreshCheatItems) {
		if (!FTGU.JEI_LOADED)
			return;
		// JEI hiding is a no-op without JEI on the classpath.
		// Add "jei-common-api" and "jei-neoforge-api" as compileOnly deps,
		// then implement IRecipeManagerPlugin to filter locked recipes.
	}

}
