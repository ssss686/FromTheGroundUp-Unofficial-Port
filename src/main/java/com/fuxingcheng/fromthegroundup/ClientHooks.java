package com.fuxingcheng.fromthegroundup;

import java.util.function.Consumer;

import com.fuxingcheng.fromthegroundup.api.technology.ITechnology;
import net.minecraft.world.entity.player.Player;

public final class ClientHooks {

	public static Consumer<Player> openResearchBook = p -> {};
	public static Consumer<ITechnology> displayToast = t -> {};
	public static Runnable clearToasts = () -> {};
	public static Runnable initResearchBookGui = () -> {};
	/** 按配置档位显示/隐藏 JEI 里的研究指南栏位；没装 JEI 时一直是这个空实现 */
	public static Runnable applyResearchGuideMode = () -> {};

}
