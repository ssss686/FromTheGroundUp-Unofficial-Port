package ftgumod.api.technology.puzzle.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ftgumod.api.inventory.InventoryCraftingPersistent;
import ftgumod.api.technology.puzzle.PuzzleMatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;

public class PuzzleGuiMatch implements IPuzzleGui {

	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("ftgumod", "textures/gui/container/research_table.png");

	private final PuzzleMatch puzzle;
	private final Container inventory;

	public PuzzleGuiMatch(PuzzleMatch puzzle, Container inventory) {
		this.puzzle = puzzle;
		this.inventory = inventory;
	}

	@Override
	public void drawForeground(AbstractContainerScreen<?> gui, GuiGraphics graphics, int mouseX, int mouseY) {
		mouseX -= gui.getGuiLeft();
		mouseY -= gui.getGuiTop();

		boolean b = !puzzle.getRecipe().getTechnology().canResearch(gui.getMinecraft().player);

		Slot slot = gui.getSlotUnderMouse();
		if (slot != null && !slot.hasItem()) {
			int index = slot.getContainerSlot();
			if (slot.container instanceof InventoryCraftingPersistent && index >= 0 && index < 9 && puzzle.getRecipe().hasHint(index)) {
				Component hint = (puzzle.getHints() == null || b) ? puzzle.getRecipe().getHint(index).getObfuscatedHint() : puzzle.getHints().get(index);
				if (hint != null && !hint.getString().isEmpty())
					graphics.renderTooltip(gui.getMinecraft().font,
							Arrays.stream(hint.getString().split("\n")).map(Component::literal).map(Component::getVisualOrderText).toList(), mouseX, mouseY);
			}
		} else if (b && mouseX >= 90 && mouseX < 112 && mouseY >= 35 && mouseY < 50) {
			List<Component> text = Collections.singletonList(Component.translatable(
					puzzle.getRecipe().getTechnology().isResearched(gui.getMinecraft().player) ? "technology.complete.already" : "technology.complete.understand",
					puzzle.getRecipe().getTechnology().getDisplayInfo().getTitle().getString()));
			graphics.renderTooltip(gui.getMinecraft().font, text.get(0), mouseX, mouseY);
		}
	}

	@Override
	public void drawBackground(AbstractContainerScreen<?> gui, GuiGraphics graphics, int mouseX, int mouseY) {
		int guiLeft = gui.getGuiLeft();
		int guiTop = gui.getGuiTop();

		graphics.blit(TEXTURE, 29 + guiLeft, 16 + guiTop, 0, 166, 54, 54);

		if (puzzle.getRecipe().getTechnology().canResearch(gui.getMinecraft().player))
			graphics.blit(TEXTURE, 90 + guiLeft, 35 + guiTop, 54, 166, 22, 15);
		else
			graphics.blit(TEXTURE, 90 + guiLeft, 35 + guiTop, 54, 181, 22, 15);

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				if (puzzle.getRecipe().hasHint(x + y * 3) && inventory.getItem(x + y * 3).isEmpty())
					graphics.blit(TEXTURE, 30 + x * 18 + guiLeft, 17 + y * 18 + guiTop, 176, 0, 16, 16);
	}

}
