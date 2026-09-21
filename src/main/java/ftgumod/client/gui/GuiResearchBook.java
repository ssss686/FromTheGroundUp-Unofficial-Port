package ftgumod.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ftgumod.Content;
import ftgumod.FTGUConfig;
import ftgumod.api.technology.ITechnology;
import ftgumod.api.technology.unlock.IUnlock;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.server.CopyTechMessage;
import ftgumod.client.FTGUClient;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyLayout;
import ftgumod.technology.TechnologyManager;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GuiResearchBook extends Screen {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation ACHIEVEMENT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("ftgumod",
			"textures/gui/achievement/achievement_background.png");
	private static final ResourceLocation STAINED_CLAY = ResourceLocation.parse(
			"textures/block/cyan_terracotta.png");
	/** 详情面板里解锁物品槽的底框，用的就是原版 task 未完成那套边框 */
	private static final ResourceLocation SLOT_FRAME = AdvancementWidgetType.UNOBTAINED
			.frameSprite(AdvancementType.TASK);
	/** 书页贴图里那块内窗：宽 224、高 155 行（内容只用得到上面 154 行，见 VIEW_H） */
	private static final int VIEW_W = 224;
	private static final int WINDOW_H = 155;
	/**
	 * 放内容能用的高度：内窗 155 行里最下面那一行是书页贴图的底色（贴图 y = 171，不透明）——
	 * 贴图整张是最后一步盖在内容上面的（见 drawResearchScreen 末尾那个 blit）。所以排版、居中、
	 * 滚动范围和剪切框一律按 154 行算，滚到底时最下面那个框的底边正好落在 154 行。
	 *
	 * 剪切框也得跟着按 154：贴图盖得住画在那一行的贴图，但盖不住 item —— 物品图标是带着深度写、
	 * 在 z = 150 画的（GuiGraphics.renderItem，也就是上面画 tooltip 时特意抬到 z = 400 的那个原因），
	 * 后面 z = 0 的书页贴图压不过它。剪切框要是留了第 155 行，就会出现"框被书页盖掉了、钻石图标
	 * 却还露在书页底边上"。原版进度页的剪切框就是内窗本身的大小（AdvancementTab 里那个
	 * enableScissor(x, y, x + 234, y + 113)，正好是它那块内窗），画不进去的 item 直接被裁掉。
	 */
	private static final int VIEW_H = WINDOW_H - 1;
	public static Map<ResourceLocation, Double> xScrollO = new HashMap<>();
	public static Map<ResourceLocation, Double> yScrollO = new HashMap<>();
	private static boolean state = true;
	private static Technology root;
	private static Technology selected;
	private static int scroll = 1;
	private final Player player;
	private final int num = 4;

	private int x_min;
	private int y_min;
	private int x_max;
	private int y_max;
	private final int imageWidth;
	private final int imageHeight;
	private double xScrollP;
	private double yScrollP;
	private double xScrollTarget;
	private double yScrollTarget;
	/** 这次拖动是不是从书页里按下去的（按在按钮上、页面外都不算） */
	private boolean dragFromPage;
	/** 原版 AdvancementScreen 的闸：第一次拖动只置位，从第二个事件开始才真的平移 */
	private boolean isScrolling;
	private int pages;
	/** 打开书（init）时算好的"当前这一页画得出来的科技"，排版、连线、命中判定都按它走 */
	private Set<Technology> visible;

	public GuiResearchBook(Player player) {
		super(Component.translatable("item.ftgumod.research_book"));
		this.player = player;

		imageWidth = 256;
		imageHeight = 202;

		if (root == null || !TechnologyManager.INSTANCE.contains(root) || !root.canResearchIgnoreResearched(player)) {
			for (Technology technology : TechnologyManager.INSTANCE.getRoots()) {
				if (technology.canResearchIgnoreResearched(player)) {
					root = technology;
					break;
				}
			}
		}

		if (!xScrollO.containsKey(root != null ? root.getRegistryName() : null))
			xScrollO.put(root.getRegistryName(), 0.0D);
		if (!yScrollO.containsKey(root.getRegistryName()))
			yScrollO.put(root.getRegistryName(), 0.0D);
	}

	@Override
	protected void init() {
		if (root == null)
			return;

		if (selected == null || !TechnologyManager.INSTANCE.contains(selected) || !selected.isResearched(player)) {
			selected = null;
			state = true;
		}

		Double xScroll = xScrollO.get(root.getRegistryName());
		Double yScroll = yScrollO.get(root.getRegistryName());
		xScrollP = xScrollTarget = xScroll != null ? xScroll : 0.0D;
		yScrollP = yScrollTarget = yScroll != null ? yScroll : 0.0D;

		clearWidgets();
		if (state) {
			// 先按"现在画得出来的科技"把这一页重排一遍再取范围：看不见的科技不占格子，
			// 父科技才会落在唯一画得出来的孩子那一行上（见 TechnologyLayout.apply）
			visible = visibleTechs();
			TechnologyLayout.apply(root, visible::contains);

			// 整块内容占的像素范围。算式必须和 drawResearchScreen 里摆框的那两个一模一样
			// （框画在格原点 -2 的位置、26 像素见方）：那边是先把格坐标乘成像素再取整，
			// 这边要是先把坐标取整再乘格子大小，落在半格的科技（y = 4.5 那种）就会少算半格 ——
			// 范围算小了，画面既会被往下怼、又会被当成"装得下"而锁死滚不动。
			int left = Integer.MAX_VALUE;
			int right = Integer.MIN_VALUE;
			int top = Integer.MAX_VALUE;
			int bottom = Integer.MIN_VALUE;

			for (Technology technology : visible) {
				int x = (int) (technology.getDisplayInfo().getX() * 28);
				int y = (int) (technology.getDisplayInfo().getY() * 27);
				left = Math.min(left, x - 2);
				right = Math.max(right, x + 24);
				top = Math.min(top, y - 2);
				bottom = Math.max(bottom, y + 24);
			}

			// x_min / y_min / x_max / y_max 就是滚动量（画面上的偏移）的允许范围，两头都算。
			// 视口是页面里那块 224 × 155（见 drawResearchScreen 的 enableScissor）。
			//
			// 照原版 AdvancementTab 的做法：第一次画的时候把整块内容摆在视口正中间
			// （原版是 scrollX = 117 - (maxX + minX) / 2，117 就是它视口宽度的一半），
			// 而且哪根轴装得下就不让那根轴滚动（原版 scroll() 外面套的 if (maxX - minX > 234)）。
			// 装不下时两头卡在内容边上，滚到头就停，不会把内容推出页面外。
			if (right - left <= VIEW_W)
				x_min = x_max = (left + right) / 2 - VIEW_W / 2;
			else {
				x_min = left; // 滚到贴左
				x_max = right - VIEW_W; // 滚到贴右
			}

			// 纵向只按 154 行算：书页贴图是最后一步整张盖在内容上面的，那张图的内窗只有 154 行
			// 是透明的，第 155 行是书页本身的底色 —— 按 155 算的话，滚到底时最下面那个框的底边
			// 正好落在那一行上，会被书页盖掉一条，看着就像框压在书页边框上。
			if (bottom - top <= VIEW_H)
				y_min = y_max = (top + bottom) / 2 - VIEW_H / 2;
			else {
				y_min = top; // 滚到贴顶
				y_max = bottom - VIEW_H; // 滚到贴底
			}

			xScrollP = xScrollTarget = Mth.clamp(xScrollP, (double) x_min, (double) x_max);
			yScrollP = yScrollTarget = Mth.clamp(yScrollP, (double) y_min, (double) y_max);

			Button pageButton = Button.builder(root.getDisplayInfo().getTitle(),
					btn -> {
						Technology first = null;
						boolean next = false;
						for (Technology technology : TechnologyManager.INSTANCE.getRoots()) {
							if (technology.canResearchIgnoreResearched(player)) {
								if (next) {
									next = false;
									root = technology;
									break;
								}
								if (first == null)
									first = technology;
							}
							if (technology == root)
								next = true;
						}
						if (next)
							root = first;
						init();
					})
					.pos((width - imageWidth) / 2 + 24, height / 2 + 74)
					.size(125, 20)
					.build();

			if (TechnologyManager.INSTANCE.getRoots().stream().filter(t -> t.canResearchIgnoreResearched(player))
					.count() < 2)
				pageButton.active = false;

			addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> {
				this.minecraft.setScreen(null);
			}).pos(width / 2 + 24, height / 2 + 74).size(80, 20).build());
			addRenderableWidget(pageButton);

			scroll = 1;
		} else {
			addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> {
				state = true;
				init();
			}).pos(width / 2 + 24, height / 2 + 74).size(80, 20).build());

			if (FTGUConfig.cachedAllowResearchCopy && selected.canCopy()) {
				Button copyButton = Button.builder(Component.translatable("gui.copy"),
						btn -> PacketDispatcher.sendToServer(new CopyTechMessage(selected)))
						.pos((width - imageWidth) / 2 + 24, height / 2 + 74)
						.size(125, 20)
						.build();
				copyButton.active = false;
				for (int i = 0; i < player.getInventory().getContainerSize(); i++)
					if (!player.getInventory().getItem(i).isEmpty()
							&& player.getInventory().getItem(i).getItem() == Content.i_parchmentEmpty.get()) {
						copyButton.active = true;
						break;
					}
				addRenderableWidget(copyButton);
			}

			pages = (int) Math.max(
					Math.ceil(((double) selected.getUnlock().stream().filter(IUnlock::isDisplayed).count()) / num), 1);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)
				|| FTGUClient.KEY_RESEARCH_BOOK.matches(keyCode, scanCode)) {
			this.minecraft.setScreen(null);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (root == null)
			return;

		// 滚动量只在鼠标事件里改（mouseScrolled / mouseDragged），改的时候顺手夹进 init() 算好的
		// 范围，所以这里不用再管

		renderBackground(guiGraphics, mouseX, mouseY, partialTick);
		drawResearchScreen(guiGraphics, mouseX, mouseY, partialTick);

		if (selected != null && state) {
			Component title = selected.getDisplayInfo().getTitle();
			Component desc = selected.getDisplayInfo().getDescription();

			int children = 0;
			for (ITechnology child : selected.getChildren())
				if (child.isRoot())
					children++;

			int i7 = mouseX + 12;
			int k7 = mouseY - 4;

			int j8 = Math.max(this.font.width(title), 120);
			int i9 = this.font.wordWrapHeight(desc, j8);
			if (selected.isResearched(player) || children > 0)
				i9 += 12;

			// Draw the panel at z=400 like vanilla tooltips, so item icons
			// (rendered at z=150 with depth write) don't cover the text.
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
			guiGraphics.fillGradient(i7 - 3, k7 - 3, i7 + j8 + 3, k7 + i9 + 3 + 12, 0xc0000000, 0xc0000000);
			guiGraphics.drawWordWrap(this.font, desc, i7, k7 + 12, j8, 0xffa0a0a0);
			if (selected.isResearched(player))
				guiGraphics.drawString(this.font, Component.translatable("technology.researched"), i7, k7 + i9 + 4,
						0xff9090ff);
			else if (children > 0)
				guiGraphics.drawString(this.font,
						Component.translatable(children == 1 ? "technology.tab" : "technology.tabs"), i7, k7 + i9 + 4,
						0xffff5555);
			guiGraphics.drawString(this.font, title, i7, k7, -1);
			guiGraphics.pose().popPose();
		} else if (selected != null && !state) {
			Component title = selected.getDisplayInfo().getTitle();
			int x1 = (width - this.font.width(title)) / 2;
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
			guiGraphics.drawString(this.font, title, x1, (height - imageHeight) / 2 + 22, 0xffffff);

			Component desc = selected.getDisplayInfo().getDescription();
			int x2 = width / 2;
			int y2 = (height - imageHeight) / 2 + 32;

			for (FormattedCharSequence line : this.font.split(desc, 211)) {
				guiGraphics.drawString(this.font, line, x2 - (this.font.width(line) / 2), y2, 0xffa0a0a0);
				y2 += this.font.lineHeight;
			}
			guiGraphics.pose().popPose();

			String s3 = scroll + "/" + pages;
			int x3 = (width + imageWidth) / 2 - this.font.width(s3);
			int y3 = (height + imageHeight) / 2;
			guiGraphics.drawString(this.font, s3, x3 - 21, y3 - 44, 0xFFFFFF);
		}

		drawTitle(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (state && root != null) {
			// 和原版进度页一样一格 16 像素、两个轴都收（原版就是 scroll(scrollX * 16, scrollY * 16)），
			// 而且一次到位：xScrollO / yScrollO 也一起写，画面才不会因为 partialTick 那层插值慢半拍。
			// 普通鼠标只有上下那一个轴，横向得触控板或者带横向滚轮的鼠标才有
			xScrollP = xScrollTarget = Mth.clamp(xScrollP - scrollX * 16.0D, (double) x_min, (double) x_max);
			yScrollP = yScrollTarget = Mth.clamp(yScrollP - scrollY * 16.0D, (double) y_min, (double) y_max);

			xScrollO.put(root.getRegistryName(), xScrollP);
			yScrollO.put(root.getRegistryName(), yScrollP);
		} else if (!state && selected != null) {
			if (scrollY < 0)
				scroll = Math.min(scroll + 1, pages);
			if (scrollY > 0)
				scroll = Math.max(scroll - 1, 1);
		}
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button != 0)
			isScrolling = false;

		if (button != 0 || !state || root == null || !dragFromPage)
			return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

		if (!isScrolling) {
			// 原版 AdvancementScreen 也是这样：第一次拖动只把闸拉上，从第二个事件开始才真的平移，
			// 免得手抖把单击当成一次小拖动
			isScrolling = true;
		} else {
			// 1:1 跟着鼠标走（原版就是 scroll(dragX, dragY)）。这里连 xScrollO / yScrollO 一起写，
			// 画面才不会因为 partialTick 那层插值比鼠标慢半拍
			xScrollP = Mth.clamp(xScrollP - dragX, (double) x_min, (double) x_max);
			yScrollP = Mth.clamp(yScrollP - dragY, (double) y_min, (double) y_max);

			xScrollTarget = xScrollP;
			yScrollTarget = yScrollP;

			xScrollO.put(root.getRegistryName(), xScrollP);
			yScrollO.put(root.getRegistryName(), yScrollP);
		}
		return true;
	}

	/** 鼠标在不在书页那块 224 × 155 的视口里（书页外面是边框和按钮） */
	private boolean inPage(double mouseX, double mouseY) {
		int k = (width - imageWidth) / 2 + 16;
		int l = (height - imageHeight) / 2 + 17;
		return mouseX >= k && mouseX < k + VIEW_W && mouseY >= l && mouseY < l + WINDOW_H;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// 左键从书页里按下去才算这次拖动；按在按钮上或者页面外，这次拖不动画面
		dragFromPage = state && button == 0 && inPage(mouseX, mouseY);
		isScrolling = false;

		if (state && button == 0 && selected != null && selected.isResearched(player)) {
			state = false;
			init();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void tick() {
		// 这里原来有一层"慢慢追上去"的缓动，现在滚轮和拖动都是一步到位（跟原版进度页一样），
		// xScrollTarget 和 xScrollP 每次都一起写，缓动成了空转，删掉；留着它只会让人以为画面会滞后
		if (root != null) {
			xScrollO.put(root.getRegistryName(), xScrollP);
			yScrollO.put(root.getRegistryName(), yScrollP);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void renderBlurredBackground(float partialTick) {
	}

	private void drawTitle(GuiGraphics guiGraphics) {
		int i = (width - imageWidth) / 2;
		int j = (height - imageHeight) / 2;
		guiGraphics.drawString(this.font, Component.translatable("item.ftgumod.research_book"), i + 15, j + 5, 0x404040, false);
	}

	private void drawResearchScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		int k = (width - imageWidth) / 2;
		int l = (height - imageHeight) / 2;
		int i1 = k + 16;
		int j1 = l + 17;

		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(i1, j1, 0F);

		ResourceLocation bg = root.getDisplayInfo().getBackground().orElse(STAINED_CLAY);
		RenderSystem.setShaderTexture(0, bg);
		for (int y = 0; y < 10; y++)
			for (int x = 0; x < 14; x++)
				guiGraphics.blit(bg, x * 16, y * 16, 0, 0, 16, 16, 16, 16);

		RenderSystem.setShaderTexture(0, ACHIEVEMENT_BACKGROUND);
		// 高度按 VIEW_H（不是内窗的 155）：第 155 行是书页自己的底边，剪切框带上它的话，
		// 物品图标会带着 z = 150 的深度从那一条里露出来（贴图盖不住 item），详见 VIEW_H 的注释
		guiGraphics.enableScissor(k + 16, l + 17, k + 16 + VIEW_W, l + 17 + VIEW_H);

		if (state) {
			Double xScrollOld = xScrollO.get(root.getRegistryName());
			Double yScrollOld = yScrollO.get(root.getRegistryName());
			double xOld = xScrollOld != null ? xScrollOld : 0.0D;
			double yOld = yScrollOld != null ? yScrollOld : 0.0D;
			int i = Mth.floor(xOld + (xScrollP - xOld) * partialTick);
			int j = Mth.floor(yOld + (yScrollP - yOld) * partialTick);

			i = Mth.clamp(i, x_min, x_max);
			j = Mth.clamp(j, y_min, y_max);

			// init() 里算好的那一份：画得出来的科技，判据见 isVisible
			Set<Technology> tech = visible;

			try {
				// 连接线照原版 AdvancementWidget.drawConnectivity 画：从父图标中心横着出去，
				// 在两框之间那条缝里拐个弯，再横着进子图标中心。先铺一遍三像素宽的黑色描边，
				// 再用本色盖一遍一像素宽的本体，看起来和进度页是同一种线：没研究的绿，研究过的白。
				//
				// 和原版有一处不一样：一个父节点挂着好几条腿的时候，主干和父节点那一行是共用的，
				// 这里按父节点整组画，顺便按注册名排个序，免得遍历顺序让画面随进程变。每个子节点
				// 各画一遍的话，后画的会把自己的颜色和黑描边盖到先画好的线上，上下分叉的地方看
				// 上去就是两条线压在一起（原版的线全是白的才看不出，这边有绿有白，一压就露馅）。
				Map<Technology, List<Technology>> branches = new TreeMap<>(
						Comparator.comparing(Technology::getRegistryName));
				// 线得两头都画得出来：画得出来的子节点，配上它往上数第一个画得出来的祖先。
				//
				// 中间夹着画不出来的科技（隐藏科技还没完成、或者那一层还没解锁）就跳过它接着往上找。
				// 只看父节点是不行的：父节点画不出来的时候整条线会被丢掉，画面上就是一个框孤零零
				// 杵在那儿没有线。跳过中间层之后，被跳过那层做完、变可见的那一刻，拐点自己就多长出
				// 一条腿来 —— 腿的条数跟着看得见的子节点走，完成前不画，完成后自动变成多分枝。
				for (Technology t1 : tech) {
					Technology parent = visibleAncestor(t1, tech);
					if (parent == null)
						continue;
					List<Technology> siblings = branches.get(parent);
					if (siblings == null)
						branches.put(parent, siblings = new ArrayList<>());
					siblings.add(t1);
				}

				// 描边和本色分两遍整片走，跟原版一样（原版是先 drawLine=true 递归一整遍、
				// 再 false 递归一整遍）。顺序反了的话，后画的那一组的黑边会切进先画的那一组
				// 的线里，看着就是一个黑豁口。
				for (Technology parent : branches.keySet())
					drawBranch(guiGraphics, parent, branches.get(parent), player, i, j, true);
				for (Technology parent : branches.keySet())
					drawBranch(guiGraphics, parent, branches.get(parent), player, i, j, false);

				selected = null;

				float f3 = mouseX - i1;
				float f4 = mouseY - j1;

				for (Technology t2 : tech) {
					int l6 = (int) (t2.getDisplayInfo().getX() * 28 - i);
					int j7 = (int) (t2.getDisplayInfo().getY() * 27 - j);
					if (l6 < -28 || j7 < -27 || l6 > 224F || j7 > 155F)
						continue;

					// 边框用原版的三套（task / goal / challenge，由科技 json 的 display.frame 指定），
					// 完成与否看研究状态 —— 原版是看进度做完没，语义对得上
					AdvancementWidgetType widgetType = t2.isResearched(player) ? AdvancementWidgetType.OBTAINED
							: AdvancementWidgetType.UNOBTAINED;
					guiGraphics.blitSprite(widgetType.frameSprite(t2.getDisplayInfo().getType()), l6 - 2, j7 - 2,
							26, 26);

					guiGraphics.renderItem(t2.getDisplayInfo().getIcon(), l6 + 3, j7 + 3);

					// 命中判定也按这一份 visible 走：画得出来的就都选得上，跟原版进度页一样，
					// 看得见就悬停给你看名字。选中只影响提示框，真要翻到物品页还得已经研究过（见 mouseClicked）
					if (f3 >= l6 && f3 <= l6 + 22 && f4 >= j7 && f4 <= j7 + 22)
						selected = t2;
				}
			} catch (ConcurrentModificationException e) {
				LOGGER.debug("Prevented ConcurrentModificationException while rendering GuiResearchBook");
			}
		} else {
			List<IUnlock> display = selected.getUnlock().stream().filter(IUnlock::isDisplayed)
					.collect(Collectors.toList());
			for (int pos = 0; pos < num; pos++) {
				int n = pos + (num * (scroll - 1));
				if (n >= display.size())
					break;

				ItemStack[] list = display.get(n).getIcon().getItems();

				if (list.length == 0)
					continue;

				long tick = player.level().getGameTime() / 30;
				int index = (int) (tick % list.length);

				ItemStack item = list[index];

				guiGraphics.blitSprite(SLOT_FRAME, 6, 37 + (pos * 28), 26, 26);

				guiGraphics.renderItem(item, 11, 42 + (pos * 28));

				boolean hovered = mouseX >= i1 + 6 && mouseX < i1 + 32 && mouseY >= j1 + 37 + (pos * 28)
						&& mouseY < j1 + 63 + (pos * 28);

				if (!hovered) {
					String name = item.getHoverName().getString().replace("[", "").replace("]", "");
					guiGraphics.drawString(this.font, name, 35, 45 + (pos * 28), 0xFFFFFF);
				} else {
					int r = 0;
					var level = player.level();
					for (RecipeHolder<?> holder : level.getRecipeManager().getRecipes()) {
						var recipe = holder.value();
						if (!(recipe instanceof net.minecraft.world.item.crafting.CraftingRecipe))
							continue;
						if (recipe instanceof net.minecraft.world.item.crafting.CustomRecipe)
							continue;
						if (ItemStack.isSameItem(item, recipe.getResultItem(level.registryAccess()))) {
							int recipeWidth = 3;
							int recipeHeight = 3;
							if (recipe instanceof ShapedRecipe shaped) {
								recipeWidth = shaped.getWidth();
								recipeHeight = shaped.getHeight();
							}

							int xp = 31 + (r * 25);
							int yp = 38 + (pos * 28);

							guiGraphics.blitSprite(ResourceLocation.withDefaultNamespace("recipe_book/crafting_overlay"), xp, yp, 24, 24);

							Iterator<Ingredient> iterator = recipe.getIngredients().iterator();

							outer: for (int yi = 0; yi < recipeHeight; ++yi) {
								int py = 3 + yi * 7;

								for (int xi = 0; xi < recipeWidth; ++xi) {
									if (iterator.hasNext()) {
										ItemStack[] stacks = iterator.next().getItems();

										if (stacks.length != 0) {
											int px = 3 + xi * 7;
											poseStack.pushPose();
											int i2 = (int) ((float) (xp + px) / 0.42F - 3.0F);
											int j2 = (int) ((float) (yp + py) / 0.42F - 3.0F);
											poseStack.scale(0.42F, 0.42F, 1.0F);
											guiGraphics.renderItem(stacks[(int) (tick % stacks.length)], i2, j2);
											poseStack.popPose();
										}
									} else
										break outer;
								}
							}
							r++;
						}
					}
					if (r == 0) {
						String name = item.getHoverName().getString().replace("[", "").replace("]", "");
						guiGraphics.drawString(this.font, name, 35, 45 + (pos * 28), 0xFFFFFF);
					}
				}
			}
		}

		// 内框渐变效果（在 scissor 区域内绘制，参考原版进度 UI）
		// 原版风格：精简层次、柔和过渡、轻薄凹陷感
		int left = 0;
		int top = 0;
		int right = 224;
		int bottom = 155;

		// 5层半透明覆盖，alpha值低，营造柔和弱阴影
		// 第1层：最内圈，alpha 80
		guiGraphics.fill(left, top, right, top + 1, 0x50000000);
		guiGraphics.fill(left, bottom - 1, right, bottom, 0x50000000);
		guiGraphics.fill(left, top, left + 1, bottom, 0x50000000);
		guiGraphics.fill(right - 1, top, right, bottom, 0x50000000);

		// 第2层：alpha 55
		guiGraphics.fill(left, top + 1, right, top + 2, 0x37000000);
		guiGraphics.fill(left, bottom - 2, right, bottom - 1, 0x37000000);
		guiGraphics.fill(left + 1, top, left + 2, bottom, 0x37000000);
		guiGraphics.fill(right - 2, top, right - 1, bottom, 0x37000000);

		// 第3层：alpha 35
		guiGraphics.fill(left, top + 2, right, top + 3, 0x23000000);
		guiGraphics.fill(left, bottom - 3, right, bottom - 2, 0x23000000);
		guiGraphics.fill(left + 2, top, left + 3, bottom, 0x23000000);
		guiGraphics.fill(right - 3, top, right - 2, bottom, 0x23000000);

		// 第4层：alpha 18
		guiGraphics.fill(left, top + 3, right, top + 4, 0x12000000);
		guiGraphics.fill(left, bottom - 4, right, bottom - 3, 0x12000000);
		guiGraphics.fill(left + 3, top, left + 4, bottom, 0x12000000);
		guiGraphics.fill(right - 4, top, right - 3, bottom, 0x12000000);

		// 第5层：最外圈，alpha 8，几乎透明
		guiGraphics.fill(left, top + 4, right, top + 5, 0x08000000);
		guiGraphics.fill(left, bottom - 5, right, bottom - 4, 0x08000000);
		guiGraphics.fill(left + 4, top, left + 5, bottom, 0x08000000);
		guiGraphics.fill(right - 5, top, right - 4, bottom, 0x08000000);

		guiGraphics.disableScissor();

		poseStack.popPose();
		RenderSystem.setShaderTexture(0, ACHIEVEMENT_BACKGROUND);
		guiGraphics.blit(ACHIEVEMENT_BACKGROUND, k, l, 0, 0, imageWidth, imageHeight, 256, 256);
	}

	/**
	 * 画一个科技到它所有子科技之间的连接线：从父图标中心横着出去，在两框之间那条缝里拐个
	 * 弯，再横着进子图标中心。一个父节点挂好几条腿的时候，主干和父节点那一行是共用的，
	 * 所以按父节点整组画，主干还要按“这一段兜着哪几条腿”分段上色 —— 这一组里还有没研究完
	 * 的腿，主干那一段就还是绿的，全研究完了才是白的。
	 *
	 * @param outline true 画三像素宽的黑描边，false 画一像素宽的本色；两遍分开整片走，
	 *                不然后一组的黑边会切进前一组的线里
	 */
	private static void drawBranch(GuiGraphics guiGraphics, Technology parent, List<Technology> siblings, Player player,
			int i, int j, boolean outline) {
		int xParent = (int) ((parent.getDisplayInfo().getX() * 28 - i) + 11);
		int yParent = (int) ((parent.getDisplayInfo().getY() * 27 - j) + 11);

		// 拐点：原版是“父格 + 30”（父框右边再出去一像素），书里的框画在 l6 - 2、
		// 比原版的 l6 + 3 整体靠左五像素，所以这里用 + 25，落点一样 —— 两框之间那
		// 两像素缝的右半边。子节点一定在父节点右边（自动排版保证），和原版的前提相同。
		int elbow = (int) (parent.getDisplayInfo().getX() * 28 - i) + 25;

		// 主干要从最上面那条腿连到最下面那条腿，中间一定经过父节点那一行
		int top = yParent;
		int bottom = yParent;
		boolean open = false;
		Map<Integer, Boolean> legs = new HashMap<>();
		for (Technology child : siblings) {
			int yChild = (int) ((child.getDisplayInfo().getY() * 27 - j) + 11);
			top = Math.min(top, yChild);
			bottom = Math.max(bottom, yChild);
			legs.put(yChild, child.isResearched(player));
			open |= !child.isResearched(player);
		}

		if (outline) {
			guiGraphics.vLine(elbow - 1, top, bottom, 0xff000000);
			guiGraphics.vLine(elbow + 1, top, bottom, 0xff000000);
			guiGraphics.hLine(elbow, xParent, yParent - 1, 0xff000000);
			guiGraphics.hLine(elbow + 1, xParent, yParent, 0xff000000);
			guiGraphics.hLine(elbow, xParent, yParent + 1, 0xff000000);
			for (Technology child : siblings) {
				int xChild = (int) ((child.getDisplayInfo().getX() * 28 - i) + 11);
				int yChild = (int) ((child.getDisplayInfo().getY() * 27 - j) + 11);
				guiGraphics.hLine(xChild, elbow - 1, yChild - 1, 0xff000000);
				guiGraphics.hLine(xChild, elbow - 1, yChild, 0xff000000);
				guiGraphics.hLine(xChild, elbow - 1, yChild + 1, 0xff000000);
			}
			return;
		}

		// 父节点那一行也是共用的：这一组还有没研究完的腿，它就还是绿的
		guiGraphics.hLine(elbow, xParent, yParent, open ? 0xff00ff00 : 0xffffffff);

		// 主干本色分段画：把父节点那一行和每条腿那一行排好，相邻两行之间算一段，
		// 从离父节点最远的一段往回走，越往近走这一段兜着的腿越多
		List<Integer> rows = new ArrayList<>(legs.keySet());
		rows.add(yParent);
		rows.sort(null);
		int parentRow = rows.indexOf(yParent);

		open = false;
		for (int step = 0; step < parentRow; step++) {
			open |= !legs.get(rows.get(step));
			guiGraphics.vLine(elbow, rows.get(step), rows.get(step + 1), open ? 0xff00ff00 : 0xffffffff);
		}

		open = false;
		for (int step = rows.size() - 1; step > parentRow; step--) {
			open |= !legs.get(rows.get(step));
			guiGraphics.vLine(elbow, rows.get(step - 1), rows.get(step), open ? 0xff00ff00 : 0xffffffff);
		}

		// 每条腿只管自己那一截：从主干拐出去，横着进子图标中心
		for (Technology child : siblings) {
			int xChild = (int) ((child.getDisplayInfo().getX() * 28 - i) + 11);
			int yChild = (int) ((child.getDisplayInfo().getY() * 27 - j) + 11);
			guiGraphics.hLine(xChild, elbow, yChild, legs.get(yChild) ? 0xffffffff : 0xff00ff00);
		}
	}

	/**
	 * 往上数第一个画得出来的祖先，跳过中间所有画不出来的。一个都没有 —— 比如中间整条链都是隐藏
	 * 科技 —— 就返回 null，这条线干脆不画。visible 是 page 的子集，所以走到页外自然就断了。
	 */
	private static Technology visibleAncestor(Technology technology, Set<Technology> visible) {
		for (Technology parent = technology.getParent(); parent != null; parent = parent.getParent())
			if (visible.contains(parent))
				return parent;
		return null;
	}

	/**
	 * 当前这一页里画得出来的科技（判据见 isVisible）。排版、连线、命中判定全用这一份，
	 * 免得几处各算各的、算岔了就会留下一个画不出来的格子或者一条连不到头的线。
	 * 页根自己永远算画得出来 —— 书本来就是挑一个能研究的根打开的。
	 */
	private Set<Technology> visibleTechs() {
		Set<Technology> page = new HashSet<>();
		root.getChildren(page, true);

		// 通向某个已经研究过的科技的那几层得留着，不然路是断的
		Set<Technology> hasResearchedDescendant = new HashSet<>();
		for (Technology technology : page)
			if (technology.isResearched(player))
				for (Technology parent = technology.getParent(); parent != null && page.contains(parent); parent = parent
						.getParent())
					if (!parent.isResearched(player) && !parent.isUnlocked(player))
						hasResearchedDescendant.add(parent);

		page.removeIf(technology -> technology != root && !isVisible(technology, player, hasResearchedDescendant));
		return page;
	}

	/**
	 * 研究之书里这个科技画不画：位置够得着（criteria 不参与）、或者通向某个已经研究过的科技；
	 * 隐藏只认科技 json 里的 display.hidden —— 跟原版进度一样，写了的科技自己没进度就不画，有了进度自己冒出来。
	 */
	private static boolean isVisible(Technology technology, Player player, Set<Technology> hasResearchedDescendant) {
		if (technology.getDisplayInfo().isHidden() && !technology.hasProgress(player))
			return false;
		return technology.canResearchIgnoreResearchedAndCustomUnlock(player)
				|| hasResearchedDescendant.contains(technology);
	}

}
