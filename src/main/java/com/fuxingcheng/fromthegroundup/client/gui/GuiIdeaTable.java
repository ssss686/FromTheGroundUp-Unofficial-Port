package com.fuxingcheng.fromthegroundup.client.gui;

import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.FTGU;
import com.fuxingcheng.fromthegroundup.inventory.ContainerIdeaTable;
import com.fuxingcheng.fromthegroundup.tileentity.TileEntityIdeaTable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class GuiIdeaTable extends AbstractContainerScreen<ContainerIdeaTable> {

	private final ResourceLocation texture;
	private final Inventory player;

	public GuiIdeaTable(ContainerIdeaTable menu, Inventory player, Component title) {
		super(menu, player, title);
		this.player = player;
		this.texture = ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "textures/gui/container/idea_table.png");
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		graphics.drawString(font, Content.b_ideaTable.getName(), imageWidth / 2 - font.width(Content.b_ideaTable.getName()) / 2, 6, 4210752, false);
		graphics.drawString(font, player.getDisplayName().getString(), 8, imageHeight - 96 + 2, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

}
