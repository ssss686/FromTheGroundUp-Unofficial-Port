package ftgumod.api.technology.puzzle.gui;

import java.util.Collections;
import java.util.List;

import ftgumod.api.technology.puzzle.ResearchConnect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;

public class PuzzleGuiConnect implements IPuzzleGui {

	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ftgumod", "textures/gui/container/research_table.png");

	private final ResearchConnect research;

	private static void renderItemTooltip(AbstractContainerScreen<?> gui, GuiGraphics graphics, net.minecraft.world.item.ItemStack stack, int x, int y) {
		graphics.renderTooltip(gui.getMinecraft().font, stack, x, y);
	}

	public PuzzleGuiConnect(ResearchConnect research) {
		this.research = research;
	}

	@Override
	public void drawForeground(AbstractContainerScreen<?> gui, GuiGraphics graphics, int mouseX, int mouseY) {
		mouseX -= gui.getGuiLeft();
		mouseY -= gui.getGuiTop();
		if (research.getTechnology().canResearch(gui.getMinecraft().player)) {
			if (mouseX >= 25 && mouseX < 43 && mouseY >= 34 && mouseY < 52)
				renderItemTooltip(gui, graphics, research.left.getDisplayStack(), mouseX, mouseY);
			if (mouseX >= 97 && mouseX < 115 && mouseY >= 34 && mouseY < 52)
				renderItemTooltip(gui, graphics, research.right.getDisplayStack(), mouseX, mouseY);
		} else if (mouseX >= 97 && mouseX < 119 && mouseY >= 35 && mouseY < 50) {
			List<Component> text = Collections.singletonList(Component.translatable(
					research.getTechnology().isResearched(gui.getMinecraft().player) ? "technology.complete.already" : "technology.complete.understand",
					research.getTechnology().getDisplayInfo().getTitle().getString()));
			graphics.renderTooltip(gui.getMinecraft().font, text.get(0), mouseX, mouseY);
		}
	}

	@Override
	public void drawBackground(AbstractContainerScreen<?> gui, GuiGraphics graphics, int mouseX, int mouseY) {
		int guiLeft = gui.getGuiLeft();
		int guiTop = gui.getGuiTop();

		graphics.blit(TEXTURE, 43 + guiLeft, 34 + guiTop, 0, 166, 54, 18);

		if (research.getTechnology().canResearch(gui.getMinecraft().player)) {
			RenderSystem.enableDepthTest();
			graphics.renderFakeItem(research.left.getDisplayStack(), 26 + guiLeft, 35 + guiTop);
			graphics.renderItemDecorations(gui.getMinecraft().font, research.left.getDisplayStack(), 26 + guiLeft, 35 + guiTop, null);

			graphics.renderFakeItem(research.right.getDisplayStack(), 98 + guiLeft, 35 + guiTop);
			graphics.renderItemDecorations(gui.getMinecraft().font, research.right.getDisplayStack(), 98 + guiLeft, 35 + guiTop, null);
		} else
			graphics.blit(TEXTURE, 97 + guiLeft, 35 + guiTop, 54, 181, 22, 15);
	}

}
