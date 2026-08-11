package ftgumod.client;

import ftgumod.ClientHooks;
import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.client.gui.GuiResearchBook;
import ftgumod.client.gui.toast.ToastTechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.client.gui.GuiIdeaTable;
import ftgumod.client.gui.GuiResearchTable;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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


	static {
		ClientHooks.openResearchBook = p -> Minecraft.getInstance().setScreen(new GuiResearchBook(p));
		ClientHooks.displayToast = t -> Minecraft.getInstance().getToasts().addToast(new ToastTechnology(t));
		ClientHooks.clearToasts = () -> Minecraft.getInstance().getToasts().clear();
		ClientHooks.initResearchBookGui = () -> {
			java.util.function.Supplier<java.util.stream.Stream<Technology>> stream = TechnologyManager.INSTANCE.getRoots()::stream;
			GuiResearchBook.zoom = stream.get().collect(java.util.stream.Collectors.toMap(Technology::getRegistryName, tech -> 1.0F));
			GuiResearchBook.xScrollO = stream.get().collect(java.util.stream.Collectors.toMap(Technology::getRegistryName, tech -> -82.0));
			GuiResearchBook.yScrollO = stream.get().collect(java.util.stream.Collectors.toMap(Technology::getRegistryName, tech -> -82.0));
		};
	}

}
