package com.fuxingcheng.fromthegroundup.util;

import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.Nullable;

/**
 * Helper class to store and retrieve the current Minecraft server instance.
 * This replaces NeoForge's ServerLifecycleHooks.
 */
public class ServerHelper {

	private static MinecraftServer currentServer;

	public static void setServer(MinecraftServer server) {
		currentServer = server;
	}

	@Nullable
	public static MinecraftServer getCurrentServer() {
		return currentServer;
	}

}
