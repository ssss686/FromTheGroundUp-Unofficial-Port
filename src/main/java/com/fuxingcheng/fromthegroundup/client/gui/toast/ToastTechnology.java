package com.fuxingcheng.fromthegroundup.client.gui.toast;

import com.fuxingcheng.fromthegroundup.api.technology.ITechnology;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ToastTechnology implements Toast {

	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("toast/advancement");

	private final ITechnology tech;

	public ToastTechnology(ITechnology tech) {
		this.tech = tech;
	}

	@Override
	public Visibility render(GuiGraphics graphics, ToastComponent toastGui, long delta) {
		graphics.blitSprite(TEXTURE, 0, 0, 160, 32);

		String display = Component.translatable("technology.toast").getString();
		String title = tech.getDisplayInfo().getTitle().getString();

		if (toastGui.getMinecraft().font.width(title) <= 125) {
			graphics.drawString(toastGui.getMinecraft().font, display, 30, 7, 0xFFFF00);
			graphics.drawString(toastGui.getMinecraft().font, title, 30, 18, -1);
		} else {
			int alpha;
			if (delta < 1500L) {
				alpha = Mth.floor(Mth.clamp((float) (1500L - delta) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 0x400000;
				graphics.drawString(toastGui.getMinecraft().font, display, 30, 11, 0xFFFF00 | alpha);
			} else {
				alpha = Mth.floor(Mth.clamp((float) (delta - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 0x400000;
				var lines = toastGui.getMinecraft().font.split(Component.literal(title), 125);
				int y = 8;
				for (var line : lines) {
					graphics.drawString(toastGui.getMinecraft().font, line, 30, y, 0xFFFFFF | alpha);
					y += 10;
				}
			}
		}

		graphics.renderFakeItem(tech.getDisplayInfo().getIcon(), 8, 8);

		return delta >= 5000L ? Visibility.HIDE : Visibility.SHOW;
	}

}
