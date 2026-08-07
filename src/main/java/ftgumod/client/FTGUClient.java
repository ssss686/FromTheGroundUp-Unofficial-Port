package ftgumod.client;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.client.gui.GuiIdeaTable;
import ftgumod.client.gui.GuiResearchTable;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = FTGU.MODID, value = Dist.CLIENT)
public final class FTGUClient {

	public static final KeyMapping KEY_RESEARCH_BOOK = new KeyMapping(
			"key.ftgumod.research_book",
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.ftgumod");

	@SubscribeEvent
	static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(Content.m_ideaTable.get(), GuiIdeaTable::new);
		event.register(Content.m_researchTable.get(), GuiResearchTable::new);
	}

	@SubscribeEvent
	static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(KEY_RESEARCH_BOOK);
	}

}
