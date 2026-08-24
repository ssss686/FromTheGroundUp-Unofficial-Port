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
		// Debug: log the coordinates to understand what's happening
		// In NeoForge, renderLabels receives screen-absolute coords, then we subtract guiLeft/guiTop
		// Let's try both approaches and see which works

		// Approach 1: Original NeoForge style - convert to GUI-relative
		int guiRelX = mouseX - guiLeft;
		int guiRelY = mouseY - guiTop;

		// Debug logging (remove after testing)
		if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.tickCount % 60 == 0) {
			FromTheGroundUp.LOGGER.debug("drawForeground: mouseX={}, mouseY={}, guiLeft={}, guiTop={}, guiRelX={}, guiRelY={}",
					mouseX, mouseY, guiLeft, guiTop, guiRelX, guiRelY);
		}

		boolean b = !puzzle.getRecipe().getTechnology().canResearch(Minecraft.getInstance().player);

		// Find slot under mouse by iterating slots (use GUI-relative coords)
		Slot slot = null;
		for (Slot s : gui.getMenu().slots) {
			if (guiRelX >= s.x && guiRelX < s.x + 16 && guiRelY >= s.y && guiRelY < s.y + 16) {
				slot = s;
				break;
			}
		}
		if (slot != null && !slot.hasItem()) {
			int index = slot.index;
			if (slot.container instanceof InventoryCraftingPersistent && index >= 0 && index < 9 && puzzle.getRecipe().hasHint(index)) {
				Component hint = (puzzle.getHints() == null || b) ? puzzle.getRecipe().getHint(index).getObfuscatedHint() : puzzle.getHints().get(index);
				if (hint != null && !hint.getString().isEmpty())
					// Try with GUI-relative coords (original NeoForge behavior)
					graphics.renderTooltip(Minecraft.getInstance().font, Minecraft.getInstance().font.split(hint, 200), guiRelX, guiRelY);
			}
		} else if (b && guiRelX >= 90 && guiRelX < 112 && guiRelY >= 35 && guiRelY < 50) {
			List<Component> text = Collections.singletonList(Component.translatable(
					puzzle.getRecipe().getTechnology().isResearched(Minecraft.getInstance().player) ? "technology.complete.already" : "technology.complete.understand",
					puzzle.getRecipe().getTechnology().getDisplayInfo().getTitle().getString()));
			graphics.renderTooltip(Minecraft.getInstance().font, text.get(0), guiRelX, guiRelY);
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
