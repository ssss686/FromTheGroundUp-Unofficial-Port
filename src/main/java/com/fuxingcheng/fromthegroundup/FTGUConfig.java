package com.fuxingcheng.fromthegroundup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class FTGUConfig {

	public static boolean cachedLoadDefaultTechnologies = true;
	public static boolean cachedGiveResearchBook = true;
	public static boolean cachedAllowResearchCopy = true;
	public static HideJeiItems cachedJeiHide = HideJeiItems.LOCKED_RECIPES;

	public enum HideJeiItems {
		NOTHING,
		LOCKED_RECIPES,
		LOCKED_RECIPES_AND_ITEMS
	}

	private static final String CONFIG_FILE = "ftgumod.properties";

	public static void load() {
		File configFile = new File(FromTheGroundUp.configFolder, CONFIG_FILE);
		Properties props = new Properties();

		if (configFile.exists()) {
			try (FileInputStream fis = new FileInputStream(configFile)) {
				props.load(fis);
			} catch (IOException e) {
				FromTheGroundUp.LOGGER.warn("Failed to load config file", e);
			}
		}

		// Read values with defaults
		cachedAllowResearchCopy = Boolean.parseBoolean(props.getProperty("allowResearchCopy", "true"));
		cachedLoadDefaultTechnologies = Boolean.parseBoolean(props.getProperty("loadDefaultTechnologies", "true"));
		cachedGiveResearchBook = Boolean.parseBoolean(props.getProperty("giveResearchBook", "true"));

		String jeiHideStr = props.getProperty("jeiHide", "LOCKED_RECIPES");
		try {
			cachedJeiHide = HideJeiItems.valueOf(jeiHideStr);
		} catch (IllegalArgumentException e) {
			cachedJeiHide = HideJeiItems.LOCKED_RECIPES;
		}

		// Save to ensure defaults are written
		save();
	}

	public static void save() {
		File configFile = new File(FromTheGroundUp.configFolder, CONFIG_FILE);
		Properties props = new Properties();

		props.setProperty("allowResearchCopy", String.valueOf(cachedAllowResearchCopy));
		props.setProperty("loadDefaultTechnologies", String.valueOf(cachedLoadDefaultTechnologies));
		props.setProperty("giveResearchBook", String.valueOf(cachedGiveResearchBook));
		props.setProperty("jeiHide", cachedJeiHide.name());

		try (FileOutputStream fos = new FileOutputStream(configFile)) {
			props.store(fos, "FromTheGroundUp Configuration");
		} catch (IOException e) {
			FromTheGroundUp.LOGGER.warn("Failed to save config file", e);
		}
	}

}
