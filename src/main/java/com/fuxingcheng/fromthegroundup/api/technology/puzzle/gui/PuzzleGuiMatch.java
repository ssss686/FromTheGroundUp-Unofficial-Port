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
		// 原版 NeoForge 逻辑: mouseX -= gui.getGuiLeft(); mouseY -= gui.getGuiTop();
		// renderLabels 传入的是屏幕绝对坐标，需要转换为 GUI 相对坐标
		int relMouseX = mouseX - guiLeft;
		int relMouseY = mouseY - guiTop;

		boolean b = !puzzle.getRecipe().getTechnology().canResearch(Minecraft.getInstance().player);

		// 原版使用 gui.getSlotUnderMouse()，Fabric 中需要手动查找
		Slot slot = findSlotUnderMouse(gui, relMouseX, relMouseY);

		if (slot != null && !slot.hasItem()) {
			int index = slot.index; // slot.getSlotIndex() 在 Mojang mappings 中是 slot.index
			if (slot.container instanceof InventoryCraftingPersistent && index >= 0 && index < 9 && puzzle.getRecipe().hasHint(index)) {
				Component hint = (puzzle.getHints() == null || b) ? puzzle.getRecipe().getHint(index).getObfuscatedHint() : puzzle.getHints().get(index);
				if (hint != null && !hint.getString().isEmpty())
					// 原版使用 GUI 相对坐标调用 renderTooltip
					graphics.renderTooltip(Minecraft.getInstance().font, Minecraft.getInstance().font.split(hint, 200), relMouseX, relMouseY);
			}
		} else if (b && relMouseX >= 90 && relMouseX < 112 && relMouseY >= 35 && relMouseY < 50) {
			List<Component> text = Collections.singletonList(Component.translatable(
					puzzle.getRecipe().getTechnology().isResearched(Minecraft.getInstance().player) ? "technology.complete.already" : "technology.complete.understand",
					puzzle.getRecipe().getTechnology().getDisplayInfo().getTitle().getString()));
			graphics.renderTooltip(Minecraft.getInstance().font, text.get(0), relMouseX, relMouseY);
		}
	}

	/**
	 * 查找鼠标下的槽位 - 替代原版的 gui.getSlotUnderMouse()
	 * 在 GUI 相对坐标系中工作
	 */
	private Slot findSlotUnderMouse(AbstractContainerScreen<?> gui, int relMouseX, int relMouseY) {
		for (Slot slot : gui.getMenu().slots) {
			// slot.x, slot.y 是 GUI 相对坐标
			if (relMouseX >= slot.x && relMouseX < slot.x + 16 && relMouseY >= slot.y && relMouseY < slot.y + 16) {
				return slot;
			}
		}
		return null;
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
