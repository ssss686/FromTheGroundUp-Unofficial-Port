package com.fuxingcheng.fromthegroundup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fuxingcheng.fromthegroundup.technology.Technology;

import net.minecraft.resources.ResourceLocation;

/**
 * Compatibility wrapper for NeoForge-style references.
 * All actual logic is in {@link FromTheGroundUp}.
 */
public class FTGU {

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(Technology.Builder.class, new Technology.Deserializer())
			.create();

	public static final String MODID = FromTheGroundUp.MODID;

	public static boolean JEI_LOADED = FromTheGroundUp.JEI_LOADED;

	public static java.io.File configFolder = FromTheGroundUp.configFolder;

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

}
