package com.Fuxingcheng.ftgumod;

import java.util.function.Consumer;

import com.Fuxingcheng.ftgumod.api.technology.ITechnology;
import net.minecraft.world.entity.player.Player;

public final class ClientHooks {

	public static Consumer<Player> openResearchBook = p -> {};
	public static Consumer<ITechnology> displayToast = t -> {};
	public static Runnable clearToasts = () -> {};
	public static Runnable initResearchBookGui = () -> {};

}
