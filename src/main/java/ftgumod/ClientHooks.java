package ftgumod;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import ftgumod.api.technology.ITechnology;
import net.minecraft.world.entity.player.Player;

public final class ClientHooks {

	public static Consumer<Player> openResearchBook = p -> {};
	public static Consumer<ITechnology> displayToast = t -> {};
	public static Runnable clearToasts = () -> {};
	public static Runnable initResearchBookGui = () -> {};
	/** 按配置档位显示/隐藏 JEI 里的研究指南栏位；没装 JEI 时一直是这个空实现 */
	public static Runnable applyResearchGuideMode = () -> {};
	/** 客户端是否连着别人的服务器（远程服、别人开的局域网服）。连着的时候配置以服务端下发的为准 */
	public static BooleanSupplier isConnectedToRemoteServer = () -> false;

}
