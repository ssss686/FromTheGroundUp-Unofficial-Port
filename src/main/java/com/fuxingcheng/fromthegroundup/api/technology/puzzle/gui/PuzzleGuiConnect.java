package com.fuxingcheng.fromthegroundup.api.technology.puzzle.gui;

import java.util.Collections;
import java.util.List;

import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.api.technology.puzzle.ResearchConnect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.systems.RenderSystem;

public class PuzzleGuiConnect implements IPuzzleGui {

	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "textures/gui/container/research_table.png");

	private final ResearchConnect research;

	private static void renderItemTooltip(AbstractContainerScreen<?> gui, GuiGraphics graphics, net.minecraft.world.item.ItemStack stack, int x, int y) {
		graphics.renderTooltip(Minecraft.getInstance().font, stack, x, y);
	}

	public PuzzleGuiConnect(ResearchConnect research) {
		this.research = research;
	}

	@Override
	public void drawForeground(AbstractContainerScreen<?> gui, GuiGraphics graphics, int mouseX, int mouseY, int guiLeft, int guiTop) {
		// Get mouse position directly from the screen
		Minecraft mc = Minecraft.getInstance();
		double actualMouseX = mc.mouseHandler.xpos();
		double actualMouseY = mc.mouseHandler.ypos();
		int scaledMouseX = (int)(actualMouseX / mc.getWindow().getGuiScale());
		int scaledMouseY = (int)(actualMouseY / mc.getWindow().getGuiScale());

		// Convert to GUI-relative
		int relX = scaledMouseX - guiLeft;
		int relY = scaledMouseY - guiTop;

		if (research.getTechnology().canResearch(mc.player)) {
			if (relX >= 25 && relX < 43 && relY >= 34 && relY < 52)
				renderItemTooltip(gui, graphics, research.left.getDisplayStack(), scaledMouseX, scaledMouseY);
			if (relX >= 97 && relX < 115 && relY >= 34 && relY < 52)
				renderItemTooltip(gui, graphics, research.right.getDisplayStack(), scaledMouseX, scaledMouseY);
		} else if (relX >= 97 && relX < 119 && relY >= 35 && relY < 50) {
			List<Component> text = Collections.singletonList(Component.translatable(
					research.getTechnology().isResearched(mc.player) ? "technology.complete.already" : "technology.complete.understand",
					research.getTechnology().getDisplayInfo().getTitle().getString()));
			graphics.renderTooltip(mc.font, text.get(0), scaledMouseX, scaledMouseY);
		}
	}

	@Override
	public void drawBackground(AbstractContainerScreen<?> gui, GuiGraphics graphics, int mouseX, int mouseY, int guiLeft, int guiTop) {
		graphics.blit(TEXTURE, 43 + guiLeft, 34 + guiTop, 0, 166, 54, 18);

		if (research.getTechnology().canResearch(Minecraft.getInstance().player)) {
			RenderSystem.enableDepthTest();
			graphics.renderFakeItem(research.left.getDisplayStack(), 26 + guiLeft, 35 + guiTop);
			graphics.renderItemDecorations(Minecraft.getInstance().font, research.left.getDisplayStack(), 26 + guiLeft, 35 + guiTop, null);

			graphics.renderFakeItem(research.right.getDisplayStack(), 98 + guiLeft, 35 + guiTop);
			graphics.renderItemDecorations(Minecraft.getInstance().font, research.right.getDisplayStack(), 98 + guiLeft, 35 + guiTop, null);
		} else
			graphics.blit(TEXTURE, 97 + guiLeft, 35 + guiTop, 54, 181, 22, 15);
	}

}
