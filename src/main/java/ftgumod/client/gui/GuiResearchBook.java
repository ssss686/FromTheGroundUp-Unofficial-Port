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
import ftgumod.technology.TechnologyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
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
			Set<Technology> tree = new HashSet<>();
			root.getChildren(tree, true);

			// 整块内容占的像素范围：图标框画在格原点 -2 的地方、26 像素见方（见
			// drawResearchScreen 里那个 blit），所以最边上的框决定范围
			int minX = (int) root.getDisplayInfo().getX();
			int maxX = minX;
			int minY = (int) root.getDisplayInfo().getY();
			int maxY = minY;

			for (Technology technology : tree) {
				int x = (int) technology.getDisplayInfo().getX();
				int y = (int) technology.getDisplayInfo().getY();
				if (x < minX)
					minX = x;
				else if (x > maxX)
					maxX = x;
				if (y < minY)
					minY = y;
				else if (y > maxY)
					maxY = y;
			}

			int left = minX * 28 - 2;
			int right = maxX * 28 + 24;
			int top = minY * 27 - 2;
			int bottom = maxY * 27 + 24;

			// x_min / y_min / x_max / y_max 就是滚动量（画面上的偏移）的允许范围，两头都算。
			// 视口是页面里那块 224 × 155（见 drawResearchScreen 的 enableScissor）。
			//
			// 照原版 AdvancementTab 的做法：第一次画的时候把整块内容摆在视口正中间
			// （原版是 scrollX = 117 - (maxX + minX) / 2，117 就是它视口宽度的一半），
			// 而且哪根轴装得下就不让那根轴滚动（原版 scroll() 外面套的 if (maxX - minX > 234)）。
			// 装不下时两头卡在内容边上，滚到头就停，不会把内容推出页面外。
			if (right - left <= 224)
				x_min = x_max = (left + right) / 2 - 112;
			else {
				x_min = left; // 滚到贴左
				x_max = right - 224; // 滚到贴右
			}

			if (bottom - top <= 155)
				y_min = y_max = (top + bottom) / 2 - 77;
			else {
				y_min = top; // 滚到贴顶
				y_max = bottom - 155; // 滚到贴底
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
		return mouseX >= k && mouseX < k + 224 && mouseY >= l && mouseY < l + 155;
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
		guiGraphics.enableScissor(k + 16, l + 17, k + 16 + 224, l + 17 + 155);

		if (state) {
			Double xScrollOld = xScrollO.get(root.getRegistryName());
			Double yScrollOld = yScrollO.get(root.getRegistryName());
			double xOld = xScrollOld != null ? xScrollOld : 0.0D;
			double yOld = yScrollOld != null ? yScrollOld : 0.0D;
			int i = Mth.floor(xOld + (xScrollP - xOld) * partialTick);
			int j = Mth.floor(yOld + (yScrollP - yOld) * partialTick);

			i = Mth.clamp(i, x_min, x_max);
			j = Mth.clamp(j, y_min, y_max);

			Set<Technology> tech = new HashSet<>();
			root.getChildren(tech, true);

			// Pre-compute techs that have at least one researched descendant,
			// so intermediate techs on the path to a granted tech stay visible
			Set<Technology> hasResearchedDescendant = new HashSet<>();
			for (Technology t : tech) {
				if (t.isResearched(player)) {
					for (Technology p = t.getParent(); p != null && tech.contains(p); p = p.getParent()) {
						if (!p.isResearched(player) && !p.isUnlocked(player))
							hasResearchedDescendant.add(p);
					}
				}
			}

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
				// 线得两头都画得出来：可见的子节点，配上同样可见的父节点（可见性见 isVisible）
				for (Technology t1 : tech) {
					if (!isVisible(t1, player, hasResearchedDescendant))
						continue;
					Technology parent = t1.getParent();
					if (parent == null || !tech.contains(parent) || !isVisible(parent, player, hasResearchedDescendant))
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
					if (!isVisible(t2, player, hasResearchedDescendant))
						continue;
					int l6 = (int) (t2.getDisplayInfo().getX() * 28 - i);
					int j7 = (int) (t2.getDisplayInfo().getY() * 27 - j);
					if (l6 < -28 || j7 < -27 || l6 > 224F || j7 > 155F)
						continue;

					RenderSystem.setShaderTexture(0, ACHIEVEMENT_BACKGROUND);
					RenderSystem.enableBlend();
					if (t2.hasCustomUnlock())
						guiGraphics.blit(ACHIEVEMENT_BACKGROUND, l6 - 2, j7 - 2, 26, 202, 26, 26,
								256, 256);
					else
						guiGraphics.blit(ACHIEVEMENT_BACKGROUND, l6 - 2, j7 - 2, 0, 202, 26, 26,
								256, 256);
					RenderSystem.disableBlend();

					guiGraphics.renderItem(t2.getDisplayInfo().getIcon(), l6 + 3, j7 + 3);

					if (f3 >= l6 && f3 <= l6 + 22 && f4 >= j7 && f4 <= j7 + 22
							&& t2.canResearchIgnoreResearched(player))
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

				RenderSystem.setShaderTexture(0, ACHIEVEMENT_BACKGROUND);
				RenderSystem.enableBlend();
				guiGraphics.blit(ACHIEVEMENT_BACKGROUND, 6, 37 + (pos * 28), 0, 202, 26, 26, 256,
						256);
				RenderSystem.disableBlend();

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

	/** 研究之书里这个科技画不画：能研究、或者通向某个已经研究过的科技；隐藏的科技自己没进度也不画 */
	private static boolean isVisible(Technology technology, Player player, Set<Technology> hasResearchedDescendant) {
		if (!technology.canResearchIgnoreResearched(player) && !hasResearchedDescendant.contains(technology))
			return false;
		return !technology.getDisplayInfo().isHidden() || technology.hasProgress(player);
	}

}
