package com.fuxingcheng.fromthegroundup.api.technology.puzzle.gui;

import java.util.Collections;
import java.util.List;

import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.api.inventory.InventoryCraftingPersistent;
import com.fuxingcheng.fromthegroundup.api.technology.puzzle.PuzzleMatch;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;

public class PuzzleGuiMatch implements IPuzzleGui {

	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "textures/gui/container/research_table.png");

	private final PuzzleMatch puzzle;
	private final Container inventory;

	public PuzzleGuiMatch(PuzzleMatch puzzle, Container inventory) {
		this.puzzle = puzzle;
		this.inventory = inventory;
	}

	@Override
	public void drawForeground(AbstractContainerScreen<?> gui, GuiGraphics graphics, int mouseX, int mouseY, int guiLeft, int guiTop) {
		// Get mouse position directly from the screen - this is always screen-absolute
		Minecraft mc = Minecraft.getInstance();
		double actualMouseX = mc.mouseHandler.xpos();
		double actualMouseY = mc.mouseHandler.ypos();
		// Scale to GUI coordinates
		int scaledMouseX = (int)(actualMouseX / mc.getWindow().getGuiScale());
		int scaledMouseY = (int)(actualMouseY / mc.getWindow().getGuiScale());

		// Convert to GUI-relative
		int relX = scaledMouseX - guiLeft;
		int relY = scaledMouseY - guiTop;

		boolean b = !puzzle.getRecipe().getTechnology().canResearch(mc.player);

		// Find slot under mouse by iterating slots
		Slot slot = null;
		for (Slot s : gui.getMenu().slots) {
			if (relX >= s.x && relX < s.x + 16 && relY >= s.y && relY < s.y + 16) {
				slot = s;
				break;
			}
		}
		if (slot != null && !slot.hasItem()) {
			int index = slot.index;
			if (slot.container instanceof InventoryCraftingPersistent && index >= 0 && index < 9 && puzzle.getRecipe().hasHint(index)) {
				Component hint = (puzzle.getHints() == null || b) ? puzzle.getRecipe().getHint(index).getObfuscatedHint() : puzzle.getHints().get(index);
				if (hint != null && !hint.getString().isEmpty())
					graphics.renderTooltip(mc.font, mc.font.split(hint, 200), scaledMouseX, scaledMouseY);
			}
		} else if (b && relX >= 90 && relX < 112 && relY >= 35 && relY < 50) {
			List<Component> text = Collections.singletonList(Component.translatable(
					puzzle.getRecipe().getTechnology().isResearched(mc.player) ? "technology.complete.already" : "technology.complete.understand",
					puzzle.getRecipe().getTechnology().getDisplayInfo().getTitle().getString()));
			graphics.renderTooltip(mc.font, text.get(0), scaledMouseX, scaledMouseY);
		}
	}

	@Override
	public void drawBackground(AbstractContainerScreen<?> gui, GuiGraphics graphics, int mouseX, int mouseY, int guiLeft, int guiTop) {
		graphics.blit(TEXTURE, 29 + guiLeft, 16 + guiTop, 0, 166, 54, 54);

		if (puzzle.getRecipe().getTechnology().canResearch(Minecraft.getInstance().player))
			graphics.blit(TEXTURE, 90 + guiLeft, 35 + guiTop, 54, 166, 22, 15);
		else
			graphics.blit(TEXTURE, 90 + guiLeft, 35 + guiTop, 54, 181, 22, 15);

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(
				com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
				com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				if (puzzle.getRecipe().hasHint(x + y * 3) && inventory.getItem(x + y * 3).isEmpty())
					graphics.blit(TEXTURE, 30 + x * 18 + guiLeft, 17 + y * 18 + guiTop, 176, 0, 16, 16);
	}

}
