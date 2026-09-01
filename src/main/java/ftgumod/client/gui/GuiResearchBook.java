package ftgumod.client.gui;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiResearchBook extends Screen {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation ACHIEVEMENT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("ftgumod",
			"textures/gui/achievement/achievement_background.png");
	private static final ResourceLocation STAINED_CLAY = ResourceLocation.parse(
			"textures/block/cyan_terracotta.png");
	public static Map<ResourceLocation, Float> zoom = new HashMap<>();
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
	private int scrolling;
	private double xLastScroll;
	private double yLastScroll;
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
		if (!zoom.containsKey(root.getRegistryName()))
			zoom.put(root.getRegistryName(), 1.0F);
	}

	@Override
	protected void init() {
		if (root == null)
			return;

		if (selected == null || !TechnologyManager.INSTANCE.contains(selected) || !selected.isResearched(player)) {
			selected = null;
			state = true;
		}

		xScrollP = xScrollTarget = xScrollO.get(root.getRegistryName());
		yScrollP = yScrollTarget = yScrollO.get(root.getRegistryName());

		clearWidgets();
		if (state) {
			Set<Technology> tree = new HashSet<>();
			root.getChildren(tree, true);

			x_min = (int) root.getDisplayInfo().getX();
			y_min = (int) root.getDisplayInfo().getY();
			x_max = (int) root.getDisplayInfo().getX();
			y_max = (int) root.getDisplayInfo().getY();

			for (Technology technology : tree) {
				if (technology.getDisplayInfo().getX() < x_min)
					x_min = (int) technology.getDisplayInfo().getX();
				else if (technology.getDisplayInfo().getX() > x_max)
					x_max = (int) technology.getDisplayInfo().getX();
				if (technology.getDisplayInfo().getY() < y_min)
					y_min = (int) technology.getDisplayInfo().getY();
				else if (technology.getDisplayInfo().getY() > y_max)
					y_max = (int) technology.getDisplayInfo().getY();
			}

			x_min = x_min * 24 - 112;
			y_min = y_min * 24 - 112;
			x_max = x_max * 24 - 77;
			y_max = y_max * 24 - 77;

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

		if (state) {
			long window = this.minecraft.getWindow().getWindow();
			if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
				int i = (width - imageWidth) / 2;
				int j = (height - imageHeight) / 2;
				int k = i + 8;
				int l = j + 17;

				if ((scrolling == 0 || scrolling == 1) && mouseX >= k && mouseX < k + 224 && mouseY >= l
						&& mouseY < l + 155) {
					if (scrolling == 0) {
						scrolling = 1;
					} else {
						xScrollP -= (float) (mouseX - xLastScroll) * zoom.get(root.getRegistryName());
						yScrollP -= (float) (mouseY - yLastScroll) * zoom.get(root.getRegistryName());

						xScrollTarget = xScrollP;
						yScrollTarget = yScrollP;

						xScrollO.put(root.getRegistryName(), xScrollP);
						yScrollO.put(root.getRegistryName(), yScrollP);
					}
					xLastScroll = mouseX;
					yLastScroll = mouseY;
				}
			} else {
				scrolling = 0;
			}

			if (xScrollTarget < x_min)
				xScrollTarget = x_min;
			if (yScrollTarget < y_min)
				yScrollTarget = y_min;
			if (xScrollTarget >= x_max)
				xScrollTarget = x_max - 1;
			if (yScrollTarget >= y_max)
				yScrollTarget = y_max - 1;
		}

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
			float f3 = zoom.get(root.getRegistryName());
			zoom.put(root.getRegistryName(),
					(float) Mth.clamp(scrollY < 0 ? f3 + 0.25F : scrollY > 0 ? f3 - 0.25F : f3, 1.0F, 2.0F));

			if (zoom.get(root.getRegistryName()) != f3) {
				float f4 = f3 * imageWidth;
				float f = f3 * imageHeight;
				float f1 = zoom.get(root.getRegistryName()) * imageWidth;
				float f2 = zoom.get(root.getRegistryName()) * imageHeight;

				xScrollP -= (f1 - f4) * 0.5F;
				yScrollP -= (f2 - f) * 0.5F;

				xScrollTarget = xScrollP;
				yScrollTarget = yScrollP;

				xScrollO.put(root.getRegistryName(), xScrollP);
				yScrollO.put(root.getRegistryName(), yScrollP);
			}
		} else if (!state && selected != null) {
			if (scrollY < 0)
				scroll = Math.min(scroll + 1, pages);
			if (scrollY > 0)
				scroll = Math.max(scroll - 1, 1);
		}
		return true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (state && button == 0 && selected != null && selected.isResearched(player)) {
			state = false;
			init();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void tick() {
		if (root != null) {
			xScrollO.put(root.getRegistryName(), xScrollP);
			yScrollO.put(root.getRegistryName(), yScrollP);
			double d0 = xScrollTarget - xScrollP;
			double d1 = yScrollTarget - yScrollP;
			if (d0 * d0 + d1 * d1 < 4D) {
				xScrollP += d0;
				yScrollP += d1;
			} else {
				xScrollP += d0 * 0.85D;
				yScrollP += d1 * 0.85D;
			}
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
			float zoomVal = zoom.get(root.getRegistryName());
			poseStack.scale(1.0F / zoomVal, 1.0F / zoomVal, 1.0F);

			int i = Mth.floor(xScrollO.get(root.getRegistryName())
					+ (xScrollP - xScrollO.get(root.getRegistryName())) * partialTick);
			int j = Mth.floor(yScrollO.get(root.getRegistryName())
					+ (yScrollP - yScrollO.get(root.getRegistryName())) * partialTick);

			if (i < x_min)
				i = x_min;
			if (j < y_min)
				j = y_min;
			if (i >= x_max)
				i = x_max - 1;
			if (j >= y_max)
				j = y_max - 1;

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
				for (Technology t1 : tech) {
					boolean t1visible = t1.canResearchIgnoreResearched(player)
							|| hasResearchedDescendant.contains(t1);
					if (!t1visible)
						continue;
					if (t1.getDisplayInfo().isHidden() && !t1.hasProgress(player))
						continue;
					Technology parent = t1.getParent();
					if (parent == null || !tech.contains(parent))
						continue;
					boolean parentVisible = parent.canResearchIgnoreResearched(player)
							|| hasResearchedDescendant.contains(parent);
					if (!parentVisible)
						continue;
					int xStart = (int) ((t1.getDisplayInfo().getX() * 24 - i) + 11);
					int yStart = (int) ((t1.getDisplayInfo().getY() * 24 - j) + 11);
					int xStop = (int) ((parent.getDisplayInfo().getX() * 24 - i) + 11);
					int yStop = (int) ((parent.getDisplayInfo().getY() * 24 - j) + 11);

					boolean flag = t1.isResearched(player);

					int l4 = flag ? 0xffa0a0a0 : 0xff00ff00;

					guiGraphics.hLine(xStart, xStop, yStart, l4);
					guiGraphics.vLine(xStop, yStart, yStop, l4);

					if (xStart > xStop)
						guiGraphics.blit(ACHIEVEMENT_BACKGROUND, xStart - 11 - 7, yStart - 5, 114,
								234, 7, 11, 256, 256);
					else if (xStart < xStop)
						guiGraphics.blit(ACHIEVEMENT_BACKGROUND, xStart + 11, yStart - 5, 107,
								234, 7, 11, 256, 256);
					else if (yStart > yStop)
						guiGraphics.blit(ACHIEVEMENT_BACKGROUND, xStart - 5, yStart - 11 - 7, 96,
								234, 11, 7, 256, 256);
					else if (yStart < yStop)
						guiGraphics.blit(ACHIEVEMENT_BACKGROUND, xStart - 5, yStart + 11, 96,
								241, 11, 7, 256, 256);
				}

				selected = null;

				float f3 = (mouseX - i1) * zoomVal;
				float f4 = (mouseY - j1) * zoomVal;

				for (Technology t2 : tech) {
					boolean t2visible = t2.canResearchIgnoreResearched(player)
							|| hasResearchedDescendant.contains(t2);
					if (!t2visible)
						continue;
					if (t2.getDisplayInfo().isHidden() && !t2.hasProgress(player))
						continue;
					int l6 = (int) (t2.getDisplayInfo().getX() * 24 - i);
					int j7 = (int) (t2.getDisplayInfo().getY() * 24 - j);
					if (l6 < -24 || j7 < -24 || l6 > 224F * zoomVal || j7 > 155F * zoomVal)
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

		guiGraphics.disableScissor();
		poseStack.popPose();
		RenderSystem.setShaderTexture(0, ACHIEVEMENT_BACKGROUND);
		guiGraphics.blit(ACHIEVEMENT_BACKGROUND, k, l, 0, 0, imageWidth, imageHeight, 256, 256);
	}

}
