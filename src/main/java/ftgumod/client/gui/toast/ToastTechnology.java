package ftgumod.client.gui.toast;

import ftgumod.api.technology.ITechnology;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToastTechnology implements Toast {

	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("toast/advancement");

	private final ITechnology tech;

	/** 挑战音效只放一次，和原版 AdvancementToast 一样 */
	private boolean playedSound;

	public ToastTechnology(ITechnology tech) {
		this.tech = tech;
	}

	@Override
	public Visibility render(GuiGraphics graphics, ToastComponent toastGui, long delta) {
		graphics.blitSprite(TEXTURE, 0, 0, 160, 32);

		String display = Component.translatable("technology.toast").getString();
		String title = tech.getDisplayInfo().getTitle().getString();

		// 挑战科技那行字用原版的粉色（AdvancementToast 里 challenge 是 0xFF88FF），普通科技还是黄色
		boolean challenge = tech.getDisplayInfo().getType() == AdvancementType.CHALLENGE;
		int labelColor = challenge ? 0xFF88FF : 0xFFFF00;

		if (toastGui.getMinecraft().font.width(title) <= 125) {
			graphics.drawString(toastGui.getMinecraft().font, display, 30, 7, labelColor);
			graphics.drawString(toastGui.getMinecraft().font, title, 30, 18, -1);
		} else {
			int alpha;
			if (delta < 1500L) {
				alpha = Mth.floor(Mth.clamp((float) (1500L - delta) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 0x400000;
				graphics.drawString(toastGui.getMinecraft().font, display, 30, 11, labelColor | alpha);
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

		// challenge 科技（json 里 frame: "challenge"）弹出来时放原版那个挑战完成的音效，
		// 位置和时机照抄 AdvancementToast：第一次真正渲染出来（delta > 0）时放，只放一次；
		// 任务/目标那两种原版是不出声的，这里也一样。进出滑动的音效由 ToastComponent 自己放，不用管。
		if (!this.playedSound && delta > 0L) {
			this.playedSound = true;
			if (challenge)
				toastGui.getMinecraft().getSoundManager()
						.play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
		}

		graphics.renderFakeItem(tech.getDisplayInfo().getIcon(), 8, 8);

		return delta >= 5000L ? Visibility.HIDE : Visibility.SHOW;
	}

}
