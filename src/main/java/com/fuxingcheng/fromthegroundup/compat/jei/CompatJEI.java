package com.fuxingcheng.fromthegroundup.compat.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.fuxingcheng.fromthegroundup.ClientHooks;
import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.FTGUConfig;
import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.api.technology.puzzle.ResearchConnect;
import com.fuxingcheng.fromthegroundup.api.technology.puzzle.ResearchMatch;
import com.fuxingcheng.fromthegroundup.api.technology.recipe.IIdeaRecipe;
import com.fuxingcheng.fromthegroundup.api.technology.recipe.IResearchRecipe;
import com.fuxingcheng.fromthegroundup.api.technology.recipe.IdeaRecipe;
import com.fuxingcheng.fromthegroundup.api.technology.unlock.IUnlock;
import com.fuxingcheng.fromthegroundup.api.util.predicate.ItemPredicate;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IScalableDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JEI 集成
 * 实现研究引导功能：在 JEI 配方界面为物品添加研究信息
 */
@JeiPlugin
public class CompatJEI implements IModPlugin {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "main");

	private static IJeiRuntime jeiRuntime;
	/**
	 * 研究指南的栏位类型。registerCategories 里才赋值 ——
	 * RecipeType 在类加载阶段初始化会让插件发现失败（见下面的自定义栏位）
	 */
	private static RecipeType<ResearchGuideEntry> researchGuideRecipeType;

	// 自定义栏位（延迟创建，避免类加载时初始化 RecipeType 导致插件发现失败）
	private ResearchGuideCategory researchGuideCategory;

	@Override
	public ResourceLocation getPluginUid() {
		return PLUGIN_ID;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		CompatJEI.jeiRuntime = jeiRuntime;
		// 配置是在文件监听线程上热重载的，靠这个钩子把档位送回客户端主线程。
		// 没装 JEI 时插件根本不会被加载，ClientHooks 里就一直是空实现。
		ClientHooks.applyResearchGuideMode = CompatJEI::applyResearchGuideMode;
		// 进世界时 JEI 才建运行时，这之前收到的档位在这里补上
		applyResearchGuideMode();
		LOGGER.info("JEI runtime available");
	}

	@Override
	public void onRuntimeUnavailable() {
		jeiRuntime = null;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		LOGGER.info("JEI registerCategories called - adding research guide category");
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		researchGuideCategory = new ResearchGuideCategory(guiHelper);
		researchGuideRecipeType = researchGuideCategory.getRecipeType();
		registration.addRecipeCategories(researchGuideCategory);
		LOGGER.info("Research guide category registered: {}", researchGuideCategory.getRecipeType());
	}

	/**
	 * 按当前档位显示/隐藏研究指南栏位。
	 *
	 * 栏位是 registerCategories 时注册的，JEI 没有运行期注销的法子，隐藏得用
	 * IRecipeManager 那对 hide/unhideRecipeCategory —— 这也正是 JEI 给进度类模组
	 * 准备的接口（它自己的调试栏位就是这么切的）。那两个方法只允许在客户端主线程
	 * 调用（JEI 里有 assertMainThread），而配置热重载是在文件监听线程上回调的，
	 * 所以这里自己转线程。
	 */
	public static void applyResearchGuideMode() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null && !minecraft.isSameThread()) {
			minecraft.execute(CompatJEI::applyResearchGuideMode);
			return;
		}

		IJeiRuntime runtime = jeiRuntime;
		if (runtime == null || researchGuideRecipeType == null)
			return;   // JEI 还没起（或已停）：运行时可用时会按档位再对一次

		IRecipeManager recipeManager = runtime.getRecipeManager();
		if (FTGUConfig.cachedResearchGuideMode == FTGUConfig.ResearchGuideMode.DISABLED)
			recipeManager.hideRecipeCategory(researchGuideRecipeType);
		else
			recipeManager.unhideRecipeCategory(researchGuideRecipeType);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		// JEI 的 tab 图标优先使用 catalyst（工作台物品），而不是 getIcon()
		// 用放大镜物品作为栏位图标
		if (researchGuideCategory != null) {
			registration.addRecipeCatalysts(researchGuideCategory.getRecipeType(),
					new ItemStack(Content.i_magnifyingGlass));
			LOGGER.info("Registered research guide catalyst: magnifying glass");
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		LOGGER.info("JEI registerRecipes called - adding research guide recipes");

		if (researchGuideCategory == null) {
			return;
		}

		// 构建物品→科技映射
		Map<Item, Technology> itemTechMap = buildItemTechMap();

		// 为每个需要研究的物品创建研究引导页面。
		// 配置档位不在这里拦：配方是启动时注册一次的，档位却能运行期改，
		// 展示哪部分一律交给 setRecipe / 页面组件现取，这样来回切档位不用重启。
		List<ResearchGuideEntry> entries = new ArrayList<>();
		for (Map.Entry<Item, Technology> entry : itemTechMap.entrySet()) {
			Technology tech = entry.getValue();
			entries.add(new ResearchGuideEntry(
					new ItemStack(entry.getKey()),
					// copy()：DisplayInfo 的字段是共享实例，不要把它交出去
					tech.getDisplayInfo().getIcon().copy(),
					tech.getDisplayInfo().getTitle(),
					buildPrerequisites(tech),
					buildIdeaDisplay(tech),
					buildResearchDisplay(tech),
					tech.hasCustomUnlock()));
		}

		registration.addRecipes(researchGuideCategory.getRecipeType(), entries);
		LOGGER.info("Added research guide recipes for {} entries", entries.size());
	}

	/**
	 * 构建物品→科技映射
	 * 遍历所有科技的 unlock 列表，记录每个物品对应的科技
	 */
	private Map<Item, Technology> buildItemTechMap() {
		Map<Item, Technology> map = new HashMap<>();

		for (Technology tech : TechnologyManager.INSTANCE) {
			for (IUnlock unlock : tech.getUnlock()) {
				// 获取该 unlock 关联的物品
				for (ItemStack stack : unlock.getIcon().getItems()) {
					if (!stack.isEmpty()) {
						map.putIfAbsent(stack.getItem(), tech);
					}
				}
			}
		}

		return map;
	}

	/**
	 * 收集前置科技链路（顺序为 根科技 → 直接前置，即阅读顺序）。
	 *
	 * 返回 Component / ItemStack 而非 String：调用 getString() 会在注册配方时
	 * 就把翻译键求值并固定下来，导致切换语言后仍显示旧语言。保留原始对象可
	 * 延迟到渲染时求值；图标同理，交由栏位在排版时绘制。
	 */
	private List<Prerequisite> buildPrerequisites(Technology tech) {
		List<Prerequisite> prerequisites = new ArrayList<>();
		Technology current = tech.getParent();

		while (current != null) {
			// copy() 不能省：DisplayInfo 的字段是共享实例，渲染时还要 withStyle（原地修改）
			prerequisites.add(new Prerequisite(
					current.getDisplayInfo().getIcon().copy(),
					current.getDisplayInfo().getTitle()));
			current = current.getParent();
		}

		// getParent() 是自下而上收集的，反转成 根 → 直接前置
		Collections.reverse(prerequisites);
		return prerequisites;
	}

	/**
	 * 构建创作台原料展示，科技没有创作台配方时返回 null。
	 *
	 * 只认 IdeaRecipe 这一种实现：IIdeaRecipe 是给附属模组用的接口，
	 * 别的实现拿不到"有哪些原料/要凑几种"，这里也就无从展示。
	 */
	@Nullable
	private static IdeaDisplay buildIdeaDisplay(Technology tech) {
		IIdeaRecipe recipe = tech.getIdeaRecipe();
		if (!(recipe instanceof IdeaRecipe idea))
			return null;
		return new IdeaDisplay(idea.getIngredients(), idea.getNeeded());
	}

	/**
	 * 构建研究台谜题展示，科技没有研究台配方时返回 null。
	 *
	 * 连线谜题的答案（两端之间的物品）不在数据里，只能画出题目给的两端；
	 * 配对谜题则整张 3×3 答案都在数据里，可以照着画。
	 */
	@Nullable
	private static ResearchDisplay buildResearchDisplay(Technology tech) {
		IResearchRecipe recipe = tech.getResearchRecipe();
		if (recipe instanceof ResearchMatch match)
			return new ResearchDisplay(ResearchDisplay.Kind.MATCH, List.of(match.getIngredients()));
		if (recipe instanceof ResearchConnect connect)
			return new ResearchDisplay(ResearchDisplay.Kind.CONNECT, List.of(connect.left, connect.right));
		return null;
	}

	public static IJeiRuntime getJeiRuntime() {
		return jeiRuntime;
	}

	/**
	 * 前置科技：图标 + 名称
	 */
	public static class Prerequisite {
		private final ItemStack icon;
		private final Component name;

		public Prerequisite(ItemStack icon, Component name) {
			this.icon = icon;
			this.name = name;
		}

		/** 与研究之书里画的是同一个图标（DisplayInfo.getIcon() 的副本） */
		public ItemStack getIcon() {
			return icon;
		}

		/** 科技名，保留 Component 以便渲染时按当前语言解析 */
		public Component getName() {
			return name;
		}
	}

	/**
	 * 创作台原料展示
	 */
	public static class IdeaDisplay {
		private final List<ItemPredicate> ingredients;
		private final int needed;

		IdeaDisplay(List<ItemPredicate> ingredients, int needed) {
			this.ingredients = ingredients;
			this.needed = needed;
		}

		/** 配方接受的材料，彼此可互换 */
		public List<ItemPredicate> getIngredients() {
			return ingredients;
		}

		public int getNeeded() {
			return needed;
		}

		/** 只要凑够 needed 种就行（而非每种都要），标签行得额外说明 */
		public boolean isChoice() {
			return needed < ingredients.size();
		}
	}

	/**
	 * 研究台谜题展示。
	 *
	 * 两种谜题的几何不一样：配对是紧凑的 3×3 槽位网格；连线只有两个槽位，
	 * 但中间要空出原版箭头的宽度，间距比槽位本身宽。
	 */
	public static class ResearchDisplay {

		public enum Kind {
			/** 把左右两端连起来 */
			CONNECT,
			/** 在 3×3 里摆出答案 */
			MATCH
		}

		/** 槽位边长，与栏位里其他槽位同源，免得两处各改各的 */
		private static final int SLOT = ResearchGuideCategory.SLOT_SIZE;
		/** 原版配方箭头 22×16（JEI 的 recipe_arrow 贴图尺寸） */
		private static final int ARROW = 22;

		private final Kind kind;
		private final List<ItemPredicate> slots;

		ResearchDisplay(Kind kind, List<ItemPredicate> slots) {
			this.kind = kind;
			this.slots = slots;
		}

		public Kind getKind() {
			return kind;
		}

		/** CONNECT 为 [左, 右]；MATCH 为 9 格，下标 = y * 3 + x（空格位是没有 Ingredient 的谓词） */
		public List<ItemPredicate> getSlots() {
			return slots;
		}

		public int getRows() {
			return kind == Kind.MATCH ? 3 : 1;
		}

		/** 整块展示的宽度 */
		public int getWidth() {
			return kind == Kind.CONNECT ? SLOT * 2 + ARROW : SLOT * 3;
		}

		/** 槽位相对整块左边的偏移 */
		public int getSlotX(int index) {
			return kind == Kind.CONNECT ? index * (SLOT + ARROW) : (index % 3) * SLOT;
		}

		public int getSlotY(int index) {
			return kind == Kind.CONNECT ? 0 : (index / 3) * SLOT;
		}

		/** 箭头相对整块左边的偏移，正好落在两槽之间空出的那段 */
		public int getArrowX() {
			return SLOT;
		}
	}

	/**
	 * 自定义研究引导栏位条目
	 */
	public static class ResearchGuideEntry {
		private final ItemStack item;
		private final ItemStack techIcon;
		private final Component techName;
		private final List<Prerequisite> prerequisites;
		@Nullable
		private final IdeaDisplay idea;
		@Nullable
		private final ResearchDisplay research;
		private final boolean customUnlock;

		public ResearchGuideEntry(ItemStack item, ItemStack techIcon, Component techName,
				List<Prerequisite> prerequisites, @Nullable IdeaDisplay idea,
				@Nullable ResearchDisplay research, boolean customUnlock) {
			this.item = item;
			this.techIcon = techIcon;
			this.techName = techName;
			this.prerequisites = prerequisites;
			this.idea = idea;
			this.research = research;
			this.customUnlock = customUnlock;
		}

		public ItemStack getItem() {
			return item;
		}

		/** 科技图标，与研究之书里画的是同一个（DisplayInfo.getIcon()） */
		public ItemStack getTechIcon() {
			return techIcon;
		}

		/** 科技名，保留 Component 以便渲染时按当前语言解析 */
		public Component getTechName() {
			return techName;
		}

		/** 前置科技链路，顺序为 根科技 → 直接前置 */
		public List<Prerequisite> getPrerequisites() {
			return prerequisites;
		}

		/** 创作台原料，没有该配方的科技为 null */
		@Nullable
		public IdeaDisplay getIdea() {
			return idea;
		}

		/** 研究台谜题，没有该配方的科技为 null */
		@Nullable
		public ResearchDisplay getResearch() {
			return research;
		}

		/** 除创作台/研究台外还另有解锁条件 */
		public boolean hasCustomUnlock() {
			return customUnlock;
		}
	}

	/**
	 * 自定义研究引导栏位
	 * 图标为放大镜，显示需要研究解锁的物品及其研究信息
	 */
	public static class ResearchGuideCategory implements IRecipeCategory<ResearchGuideEntry> {

		// 页面尺寸。整页就是一个滚动视口：内容按自然高度排，装不下就整页一起滚，
		// 不会像固定分栏那样把某一块压扁。
		//
		// 高度必须塞得进 JEI 给配方留的那块地方，否则整页会挂到区域外面：
		// 配方矩形连同它那圈 4 像素边框就是 JEI 画底板用的尺寸，超出部分会盖到
		// 下面的按钮上（RecipeGuiLayouts 只在装得下时才居中，装不下就直接顶格画）；
		// 滚动条贴着整页底边，于是也跟着跑出屏幕，滑块就拖不到底。
		// 那块地方的高度 = 配方界面高 - 40（RecipeGuiSizing 里是 屏幕高 - 76，
		// 下限 175；再减去 RecipesGui 的 headerHeight(32) + borderPadding(6) + navBarPadding(2)），
		// 最小的窗口下也有 135。JEI 最高的内置配方 Information 是 125，
		// 连边框共 133 —— 这里取 126（加边框 134），再小的窗口也装得下。
		private static final int WIDTH = 170;
		private static final int HEIGHT = 126;

		/** 槽位边长 */
		private static final int SLOT_SIZE = 18;
		/**
		 * 槽位的底图比内容区向外扩 1 像素：addSlot/setPosition 定位的是 16×16 的物品区，
		 * 而 setStandardSlotBackground() 画的框是 18×18、相对物品区偏移 (-1,-1)。
		 * 排版一律以"底图"为准，落位时再补上这个内缩量。
		 */
		private static final int SLOT_INSET = (SLOT_SIZE - 16) / 2;

		/** 小标题行高：文字 9 + 行距 2 */
		private static final int LABEL_HEIGHT = 11;
		/** 纯文字行高，与 JEI 的 DrawableWrappedText 一致 */
		private static final int TEXT_ROW_HEIGHT = 11;
		private static final int SECTION_GAP = 3;

		// 滚动条整套照抄 JEI 的 AbstractScrollWidget：轨道 14 宽、滑块左右各内缩 1 像素、
		// 滑块最小 14 高，连贴图也用 JEI 自己那两张，看着才是 JEI 原生的滚动条
		private static final int SCROLLBAR_WIDTH = 14;
		/** 轨道与内容区之间的空隙（JEI 的 getScrollBoxScrollbarExtraWidth 里那 2 像素） */
		private static final int SCROLLBAR_PADDING = 2;
		private static final int SCROLLBAR_EXTRA = SCROLLBAR_WIDTH + SCROLLBAR_PADDING;
		/** 滑块相对轨道左右各内缩的像素，上下同理 */
		private static final int SCROLLBAR_INSET = 1;
		private static final int MIN_SCROLLBAR_MARKER = 14;
		/** JEI 自己的 GUI 图集，滚动条那两张九宫格贴图就在里面 */
		private static final ResourceLocation JEI_GUI_ATLAS = ResourceLocation.fromNamespaceAndPath("jei",
				"textures/atlas/gui.png");
		private static final ResourceLocation JEI_SCROLLBAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath("jei",
				"scrollbar_background_v2");
		private static final ResourceLocation JEI_SCROLLBAR_MARKER = ResourceLocation.fromNamespaceAndPath("jei",
				"scrollbar_marker_v2");
		/** 万一取不到 JEI 的贴图，退回这两个纯色 */
		private static final int SCROLLBAR_TRACK_COLOR = 0x40000000;
		private static final int SCROLLBAR_MARKER_COLOR = 0xFFC6C6C6;
		/** 滚轮一格滚多少像素（JEI 的 smoothScrollRate 默认就是 9） */
		private static final int SCROLL_STEP = 9;

		/** 内容区宽度（扣掉滚动条） */
		private static final int CONTENT_WIDTH = WIDTH - SCROLLBAR_EXTRA;

		/** 内容末尾的留白：滚到底时最后一行不贴着底板 */
		private static final int CONTENT_BOTTOM_GAP = 4;

		// 以下都是内容坐标（即滚动前）的纵向位置
		/** 页首物品槽 */
		private static final int ITEM_SLOT_Y = 1;
		// 表头："此物品需要研究 [图标] X 科技才能解锁"。
		// 图标嵌在 18×18 的槽位底图里（与页首物品槽同款），所以第一行比文字行高；
		// 折行后的续行从槽位底边开始。语言不同折行数不同，这里按两行预留。
		private static final int HEADER_Y = 21;
		private static final int HEADER_SLOT_SIZE = 18;
		private static final int HEADER_ICON_SIZE = 16;
		private static final int HEADER_ICON_OFFSET = (HEADER_SLOT_SIZE - HEADER_ICON_SIZE) / 2;
		private static final int HEADER_ICON_GAP = 2;
		private static final int HEADER_LINE2_Y = HEADER_Y + HEADER_SLOT_SIZE;
		/**
		 * 表头里图标后面至少要留出的文字宽度。留不够就别把图标塞在第一行了，
		 * 否则长前缀会把科技名挤成一行一个字（英文前缀就有 130 多像素）。
		 */
		private static final int MIN_TAIL_WIDTH = 40;

		/** 表头按两行预留后的下边界，也是正文的起始 y */
		private static final int CONTENT_TOP = HEADER_LINE2_Y + 9;

		/** 创作台原料一行能摆几个槽位（8 × 18 = 144，内容区 154 宽，左右各余 5） */
		private static final int IDEA_COLUMNS = CONTENT_WIDTH / SLOT_SIZE;

		/** 链路条目带图标，行高取槽位底图高度，文字在行内垂直居中 */
		private static final int ICON_ROW_HEIGHT = HEADER_SLOT_SIZE;
		/** 带图标条目的占位宽度（槽位宽 + 间隔） */
		private static final int ICON_ADVANCE = HEADER_SLOT_SIZE + HEADER_ICON_GAP;

		// 槽位名：createRecipeExtras 里按名字把槽位从 JEI 手上认领回来，
		// 这样就不依赖 setRecipe 的创建顺序
		private static final String SLOT_ITEM = "item";
		private static final String SLOT_IDEA = "idea";
		private static final String SLOT_PUZZLE = "puzzle";

		// JEI 的配方底板是浅灰的（single_recipe_background_v2 就是 #C6C6C6），正文得用深色。
		// 这个纯黑就是 JEI 自己画文字用的颜色（DrawableWrappedText）；
		// 带样式的组件以样式里的颜色为准，这里只是没上色时的兜底。
		private static final int FALLBACK_TEXT_COLOR = 0xFF000000;

		private final RecipeType<ResearchGuideEntry> recipeType;
		private final IDrawable icon;
		private final Component title;
		/** 页面组件要的东西都从它现取：槽位底图、配方箭头、JEI 自己的滚动条贴图 */
		private final IGuiHelper guiHelper;

		public ResearchGuideCategory(IGuiHelper guiHelper) {
			this.recipeType = RecipeType.create(FromTheGroundUp.MODID, "research_guide", ResearchGuideEntry.class);
			this.icon = guiHelper.createDrawableItemStack(
					new ItemStack(Content.i_magnifyingGlass));
			this.title = Component.translatable("ftgu.jei.research_guide");
			this.guiHelper = guiHelper;
		}

		@Override
		public RecipeType<ResearchGuideEntry> getRecipeType() {
			return recipeType;
		}

		@Override
		public Component getTitle() {
			return title;
		}

		@Override
		public IDrawable getIcon() {
			return icon;
		}

		@Override
		public int getWidth() {
			return WIDTH;
		}

		@Override
		public int getHeight() {
			return HEIGHT;
		}

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, ResearchGuideEntry entry, IFocusGroup focuses) {
			// 页首物品槽居中（JEI 内置 Information 栏位也是 77,1 —— 物品槽本来就是 18 宽）
			builder.addSlot(RecipeIngredientRole.RENDER_ONLY, centeredStart(1, CONTENT_WIDTH), ITEM_SLOT_Y)
					.setSlotName(SLOT_ITEM)
					.setStandardSlotBackground()
					.addItemStack(entry.getItem());

			// 输出关联（不可见）- 让 JEI 在物品的 tabs 中关联到此栏位。
			// 不设名字、也不交给页面组件：它还归 JEI 管，这样才挂得到物品上。
			builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
					.addItemStack(entry.getItem());

			// 研究方法展示（创作台原料、研究台谜题）：只看科技链的档位不带这些槽位。
			// 必须和页面组件一起拦 —— 没交给页面组件的槽位 JEI 会留在自己的坐标上画，
			// 于是全堆到左上角去。档位运行期可改，而 setRecipe 每次重建布局都会重跑，
			// 所以档位在这里现取。
			if (showsResearchMethods()) {
				// 创作台原料
				IdeaDisplay idea = entry.getIdea();
				if (idea != null) {
					List<ItemPredicate> ingredients = idea.getIngredients();
					for (int i = 0; i < ingredients.size(); i++) {
						addDisplaySlot(builder, SLOT_IDEA + i, ingredients.get(i));
					}
				}

				// 研究台谜题：配对是整张 3×3（空格位只画底图），连线只有两端
				ResearchDisplay research = entry.getResearch();
				if (research != null) {
					List<ItemPredicate> slots = research.getSlots();
					for (int i = 0; i < slots.size(); i++) {
						addDisplaySlot(builder, SLOT_PUZZLE + i, slots.get(i));
					}
				}
			}
		}

		/** 档位是否要展示创作台/研究台这类"研究方法"；只看科技链的档位就跳过 */
		private static boolean showsResearchMethods() {
			return FTGUConfig.cachedResearchGuideMode != FTGUConfig.ResearchGuideMode.CHAIN_ONLY;
		}

		@Override
		public void createRecipeExtras(IRecipeExtrasBuilder builder, ResearchGuideEntry entry,
				IFocusGroup focuses) {
			ResearchGuidePage page = new ResearchGuidePage(entry, builder.getRecipeSlots(), guiHelper);
			// 槽位交给页面组件：JEI 会把它们从默认绘制里摘掉（也就不会按 setRecipe 给的位置画），
			// 改由页面按滚动后的位置绘制、并按滚动状态判定悬停
			builder.addSlottedWidget(page, page.getClaimedSlots());
			// 滚轮、拖动滚动条也得能送到页面手里
			builder.addInputHandler(page);
		}

		/** n 个槽位在给定宽度里横向居中时，整块底图左边缘的 x（落位还要再加 SLOT_INSET） */
		private static int centeredStart(int columns, int areaWidth) {
			return (areaWidth - columns * SLOT_SIZE) / 2;
		}

		/** 向上取整的行数 */
		private static int rowsOf(int count, int columns) {
			return (count + columns - 1) / columns;
		}

		/**
		 * 展示用槽位。用 RENDER_ONLY：能悬停看材料、能点进去查，但不参与
		 * "这个配方用到/产出什么" 的索引，免得这些展示物品污染 JEI 的配方查找。
		 *
		 * 位置先给 (0,0)：真正的坐标由页面组件绘制时按滚动位置设置，
		 * 这里只负责起名字，让页面能按名字把槽位认领回去。
		 *
		 * 谓词没有 Ingredient 时（配对谜题里表示空位的 ItemLambda 就是这种）
		 * 只画底图不加材料 —— 空格位画个空槽才看得出网格是 3×3。
		 */
		private static void addDisplaySlot(IRecipeLayoutBuilder builder, String name, ItemPredicate predicate) {
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 0, 0)
					.setSlotName(name)
					.setStandardSlotBackground();
			Ingredient ingredient = predicate.getIngredient();
			if (ingredient != null) {
				ItemStack[] matching = ingredient.getItems();
				if (matching.length > 0)
					slot.addItemStacks(List.of(matching));
			}
		}

		/**
		 * 整页排版 + 滚动。
		 *
		 * 为什么自己写而不是用 JEI 的 {@code addScrollBoxWidget}：那个只能滚一个 IDrawable，
		 * 槽位滚不了（没交出去的槽位由 JEI 画在固定坐标上）。而
		 * {@code IRecipeSlotDrawable.setPosition} 是公开 API，配上 {@code addSlottedWidget}
		 * 就能让槽位跟着一起滚 —— JEI 自己的滚动网格也是这么做的，只是它的网格布局
		 * 摆不出"页首物品槽 + 原料行 + 3×3 谜题"这种混排。
		 *
		 * 位置写死成 (0,0) 与整页大小：JEI 传给 widget 的鼠标坐标已经减掉了
		 * getPosition()，输入派发也是先按 getArea() 做包含判断，与 JEI 内部那些滚动组件一致。
		 */
		private static final class ResearchGuidePage implements ISlottedRecipeWidget, IJeiInputHandler {

			/** 交给本组件摆放的槽位，坐标是内容坐标 */
			private record PlacedSlot(IRecipeSlotDrawable slot, int x, int y) {
			}

			private final List<PlacedSlot> slots = new ArrayList<>();
			private final List<IRecipeSlotDrawable> claimedSlots = new ArrayList<>();
			private final List<Row> rows = new ArrayList<>();
			/** 表头科技图标背后的槽位底图，与上方物品槽的 setStandardSlotBackground() 同款 */
			private final IDrawable slotDrawable;
			/** 连线谜题的箭头，与 JEI 内置配方里那个同源 */
			private final IDrawable recipeArrow;
			/** JEI 自己的滚动条贴图；取不到就是 null，绘制时退回纯色块 */
			@Nullable
			private final IScalableDrawable scrollbarBackground;
			@Nullable
			private final IScalableDrawable scrollbarMarker;
			private final int contentHeight;

			/** 已滚动距离（像素） */
			private double scrollY;
			/** 拖动时鼠标相对滑块顶边的偏移；负数表示没在拖 */
			private double dragOffsetY = -1;
			/** 连线谜题箭头的横向位置；-1 表示没有箭头可画 */
			private int arrowX = -1;
			/** 箭头的内容坐标 y */
			private int arrowY;

			ResearchGuidePage(ResearchGuideEntry entry, IRecipeSlotDrawablesView slotsView, IGuiHelper guiHelper) {
				this.slotDrawable = guiHelper.getSlotDrawable();
				this.recipeArrow = guiHelper.getRecipeArrow();
				this.scrollbarBackground = jeiSprite(guiHelper, JEI_SCROLLBAR_BACKGROUND);
				this.scrollbarMarker = jeiSprite(guiHelper, JEI_SCROLLBAR_MARKER);

				Font font = Minecraft.getInstance().font;
				// 档位现取：档位改了以后 JEI 重建布局时会重新构造本组件。
				// 与 setRecipe 用的是同一个判断，两边都不带这些槽位，才对得上
				boolean methods = showsResearchMethods();

				claim(slotsView, SLOT_ITEM, centeredStart(1, CONTENT_WIDTH) + SLOT_INSET, ITEM_SLOT_Y);
				int y = Math.max(appendHeader(entry, font), CONTENT_TOP);

				// 创作台原料
				IdeaDisplay idea = methods ? entry.getIdea() : null;
				if (idea != null) {
					List<ItemPredicate> ingredients = idea.getIngredients();

					List<Cell> label = new ArrayList<>();
					label.add(textCell(Component.translatable("ftgu.jei.research_guide.idea")
							.withStyle(ChatFormatting.BLACK), font));
					if (idea.isChoice()) {
						// "任选 N 种"贴右边：中间垫一段空白把它顶过去
						TextCell note = textCell(Component
								.translatable("ftgu.jei.research_guide.idea.needed", idea.getNeeded())
								.withStyle(ChatFormatting.DARK_GRAY), font);
						label.add(new SpacerCell(
								Math.max(CONTENT_WIDTH - label.get(0).width() - note.width(), 0)));
						label.add(note);
					}
					this.rows.add(Row.of(y, label, LABEL_HEIGHT));
					y += LABEL_HEIGHT;

					for (int i = 0; i < ingredients.size(); i++) {
						int row = i / IDEA_COLUMNS;
						// 每行各自居中：最后一行通常摆不满
						int columns = Math.min(IDEA_COLUMNS, ingredients.size() - row * IDEA_COLUMNS);
						int startX = centeredStart(columns, CONTENT_WIDTH) + SLOT_INSET;
						claim(slotsView, SLOT_IDEA + i, startX + (i % IDEA_COLUMNS) * SLOT_SIZE,
								y + row * SLOT_SIZE + SLOT_INSET);
					}
					y += rowsOf(ingredients.size(), IDEA_COLUMNS) * SLOT_SIZE + SECTION_GAP;
				}

				// 研究台谜题
				ResearchDisplay research = methods ? entry.getResearch() : null;
				if (research != null) {
					this.rows.add(Row.text(y,
							textCell(Component.translatable("ftgu.jei.research_guide.puzzle")
									.withStyle(ChatFormatting.BLACK), font)));
					int slotsY = y + LABEL_HEIGHT;
					int startX = (CONTENT_WIDTH - research.getWidth()) / 2;
					List<ItemPredicate> predicates = research.getSlots();
					for (int i = 0; i < predicates.size(); i++) {
						claim(slotsView, SLOT_PUZZLE + i,
								startX + SLOT_INSET + research.getSlotX(i),
								slotsY + SLOT_INSET + research.getSlotY(i));
					}
					if (research.getKind() == ResearchDisplay.Kind.CONNECT) {
						// 连线谜题的答案是两端之间的物品，数据里没有，画个箭头表示"从左连到右"
						this.arrowX = startX + research.getArrowX();
						this.arrowY = slotsY + (SLOT_SIZE - recipeArrow.getHeight()) / 2;
					}
					y = slotsY + research.getRows() * SLOT_SIZE + SECTION_GAP;
				}

				// 前置科技链路
				List<Prerequisite> prerequisites = entry.getPrerequisites();
				boolean custom = methods && entry.hasCustomUnlock();
				if (!prerequisites.isEmpty()) {
					this.rows.add(Row.text(y,
							textCell(Component.translatable("ftgu.jei.research_guide.prerequisites")
									.withStyle(ChatFormatting.BLACK), font)));
					y = appendPrerequisiteChain(y + LABEL_HEIGHT, prerequisites, font);
					if (custom)
						y += TEXT_ROW_HEIGHT;   // 与附加条件之间空一行
				}

				// 附加解锁条件（requirements）：不是靠创作台或研究台解锁的那部分，
				// 没有配方数据可展示，只能照实说明
				if (custom) {
					Component line = Component.translatable("ftgu.jei.research_guide.custom")
							.withStyle(ChatFormatting.DARK_GRAY);
					for (FormattedCharSequence sequence : font.split(line, CONTENT_WIDTH)) {
						this.rows.add(Row.text(y, new TextCell(sequence, font.width(sequence))));
						y += TEXT_ROW_HEIGHT;
					}
				}

				this.contentHeight = y + CONTENT_BOTTOM_GAP;
			}

			List<IRecipeSlotDrawable> getClaimedSlots() {
				return claimedSlots;
			}

			/**
			 * 取 JEI 图集里的一张九宫格贴图。
			 *
			 * 只能走公开 API：Internal.getTextures() 那一套都在 mezz.jei.common 里，
			 * 本模组只编译依赖 API jar，拿不到。好在 JEI 的图集是它自己用
			 * TextureAtlasHolder 注册进 TextureManager 的（就是这张 gui.png），
			 * 从纹理管理器里认出来再交给 IGuiHelper 包成可缩放的贴图即可。
			 *
			 * 图集还没就绪时返回 null，绘制时退回纯色块。
			 */
			@Nullable
			private static IScalableDrawable jeiSprite(IGuiHelper guiHelper, ResourceLocation sprite) {
				if (Minecraft.getInstance().getTextureManager()
						.getTexture(JEI_GUI_ATLAS) instanceof TextureAtlas atlas)
					return guiHelper.createScalableDrawableSprite(atlas, sprite);
				return null;
			}

			/** 认领一个槽位并记下它的内容坐标；名字对不上就跳过（不该发生） */
			private void claim(IRecipeSlotDrawablesView slotsView, String name, int x, int y) {
				slotsView.findSlotByName(name).ifPresent(slot -> {
					this.claimedSlots.add(slot);
					this.slots.add(new PlacedSlot(slot, x, y));
				});
			}

			/**
			 * 表头：前缀文字 → 科技图标 → 科技名 + 后缀。
			 *
			 * 为什么不用 Component 内嵌图标：1.21.1 的 Component contents 里没有内嵌贴图那一档
			 * （ObjectContents 是 1.21.4+ 才有的），NeoForge 也没提供自定义字形注册。
			 *
			 * 为什么在这里量文字：本组件是在 createRecipeExtras 里构造的，每次构建配方页面
			 * 都会重来，所以切语言会跟着重算，与 JEI 自己折行的时机一致。
			 *
			 * @return 表头排完后的 y。折行数随语言变（同一句话中英文能差出好几行），
			 *         按固定行数预留迟早会跟正文叠上；整页能滚动，多出来的行把正文往下推就是了。
			 */
			private int appendHeader(ResearchGuideEntry entry, Font font) {
				// 前后缀是说明文字，科技名是正文：全篇不加粗（JEI 自己的配方面板也不加粗），
				// 层级只靠颜色分，所以说明用深灰、内容用黑，名字才跳得出来
				Component prefix = Component.translatable("ftgu.jei.research_guide.requires.prefix")
						.withStyle(ChatFormatting.DARK_GRAY);
				Component suffix = Component.translatable("ftgu.jei.research_guide.requires.suffix")
						.withStyle(ChatFormatting.DARK_GRAY);
				// copy() 不能省：DisplayInfo.getTitle() 返回共享实例，withStyle 又是原地修改
				Component tail = entry.getTechName().copy().withStyle(ChatFormatting.BLACK).append(suffix);

				TextCell prefixCell = textCell(prefix, font);
				boolean hasIcon = !entry.getTechIcon().isEmpty();
				// 图标跟在前缀后面时，后面至少还要留得下 MIN_TAIL_WIDTH 宽的文字。
				// 不然遇上英文那种长前缀（"This item requires the " 就占去 130 多像素），
				// 科技名会被挤成一列宽度个位数的碎片，一行蹦一个字。
				boolean iconInline = hasIcon
						&& prefixCell.width() + ICON_ADVANCE + MIN_TAIL_WIDTH <= CONTENT_WIDTH;

				// 图标占的宽度只有内嵌那一行算数，续行从左边重新起
				int tailWidth = CONTENT_WIDTH - (iconInline ? prefixCell.width() + ICON_ADVANCE
						: hasIcon ? ICON_ADVANCE : 0);
				List<FormattedCharSequence> tailLines = font.split(tail, Math.max(tailWidth, 1));

				List<Cell> firstLine = new ArrayList<>();
				firstLine.add(prefixCell);
				int firstTailIndex = 0;
				if (iconInline) {
					firstLine.add(new IconCell(entry.getTechIcon(), slotDrawable));
					if (!tailLines.isEmpty()) {
						FormattedCharSequence sequence = tailLines.get(0);
						firstLine.add(new TextCell(sequence, font.width(sequence)));
						firstTailIndex = 1;
					}
				}
				// 第一行按图标的高度留，纯文字时文字在里面垂直居中，两种情况下文字位置一样
				this.rows.add(Row.of(HEADER_Y, firstLine, HEADER_SLOT_SIZE));

				// 续行从图标底边开始。带图标的那行要按图标高度让位，
				// 不然 18 高的底图会压到下一行文字上（行距只有 11）
				int offset = 0;
				for (int i = firstTailIndex; i < tailLines.size(); i++) {
					List<Cell> cells = new ArrayList<>();
					boolean withIcon = i == 0 && !iconInline && hasIcon;
					if (withIcon)
						cells.add(new IconCell(entry.getTechIcon(), slotDrawable));
					FormattedCharSequence sequence = tailLines.get(i);
					cells.add(new TextCell(sequence, font.width(sequence)));
					int height = withIcon ? ICON_ROW_HEIGHT : TEXT_ROW_HEIGHT;
					this.rows.add(Row.of(HEADER_LINE2_Y + offset, cells, height));
					offset += height;
				}
				return HEADER_LINE2_Y + offset;
			}

			/**
			 * 前置链路按"图标 + 名称"横向排布，放不下就换行，返回排完后的 y。
			 *
			 * 箭头挂在下一个条目之前，而不是上一行的行尾：折行后行首是"→"，
			 * 接续关系一眼可见（行尾箭头会像是指向屏幕外）。
			 */
			private int appendPrerequisiteChain(int y, List<Prerequisite> prerequisites, Font font) {
				List<Cell> cells = new ArrayList<>();
				int used = 0;

				for (int i = 0; i < prerequisites.size(); i++) {
					Prerequisite prerequisite = prerequisites.get(i);

					TextCell arrow = (i == 0) ? null
							: textCell(Component.literal(" → ").withStyle(ChatFormatting.DARK_GRAY), font);
					ItemStack icon = prerequisite.getIcon();
					IDrawable slot = icon.isEmpty() ? null : slotDrawable;
					// copy() 不能省：DisplayInfo.getTitle() 返回共享实例，withStyle 又是原地修改
					Component name = prerequisite.getName().copy().withStyle(ChatFormatting.BLACK);
					TextCell nameCell = textCell(name, font);

					int prefixWidth = (arrow == null ? 0 : arrow.width())
							+ (slot == null ? 0 : ICON_ADVANCE);

					if (used > 0 && used + prefixWidth + nameCell.width() > CONTENT_WIDTH) {
						// 本行剩余空间不够，条目另起一行
						this.rows.add(Row.of(y, cells, ICON_ROW_HEIGHT));
						y += ICON_ROW_HEIGHT;
						cells = new ArrayList<>();
						used = 0;
					}

					if (prefixWidth + nameCell.width() > CONTENT_WIDTH) {
						// 独占一行仍放不下 → 科技名过长，折行，续行不再重复图标
						List<FormattedCharSequence> nameLines = font.split(name,
								Math.max(CONTENT_WIDTH - prefixWidth, 1));
						for (int line = 0; line < nameLines.size(); line++) {
							List<Cell> lineCells = new ArrayList<>();
							if (line == 0) {
								if (arrow != null)
									lineCells.add(arrow);
								if (slot != null)
									lineCells.add(new IconCell(icon, slot));
							}
							FormattedCharSequence sequence = nameLines.get(line);
							lineCells.add(new TextCell(sequence, font.width(sequence)));
							this.rows.add(Row.of(y, lineCells, ICON_ROW_HEIGHT));
							y += ICON_ROW_HEIGHT;
						}
						cells = new ArrayList<>();
						used = 0;
						continue;
					}

					if (arrow != null) {
						cells.add(arrow);
						used += arrow.width();
					}
					if (slot != null) {
						cells.add(new IconCell(icon, slot));
						used += ICON_ADVANCE;
					}
					cells.add(nameCell);
					used += nameCell.width();
				}

				if (!cells.isEmpty()) {
					this.rows.add(Row.of(y, cells, ICON_ROW_HEIGHT));
					y += ICON_ROW_HEIGHT;
				}
				return y;
			}

			@Override
			public ScreenPosition getPosition() {
				return new ScreenPosition(0, 0);
			}

			@Override
			public ScreenRectangle getArea() {
				return new ScreenRectangle(0, 0, WIDTH, HEIGHT);
			}

			@Override
			public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
				Font font = Minecraft.getInstance().font;

				// 槽位位置每帧重设：JEI 判定槽位悬停读的就是这个位置，
				// getSlotUnderMouse 也拿它做命中测试，所以必须跟滚动同步
				for (PlacedSlot placed : slots) {
					placed.slot().setPosition(placed.x(), (int) Math.round(placed.y() - scrollY));
				}

				// 滚动裁剪。pose 到这里已经平移到本组件的绝对位置了，
				// 而 enableScissor 收的是绝对 GUI 坐标，所以得把矩形自己变换过去
				//（JEI 自己的 ScrollBoxRecipeWidget 也是这么干的）。
				Matrix4f pose = guiGraphics.pose().last().pose();
				Vector3f topLeft = pose.transformPosition(new Vector3f(0.0F, 0.0F, 1.0F));
				Vector3f bottomRight = pose.transformPosition(new Vector3f(WIDTH, HEIGHT, 1.0F));
				guiGraphics.enableScissor(
						Math.round(topLeft.x), Math.round(topLeft.y),
						Math.round(bottomRight.x), Math.round(bottomRight.y));
				try {
					for (Row row : rows) {
						row.draw(guiGraphics, font, scrollY);
					}
					if (arrowX >= 0) {
						recipeArrow.draw(guiGraphics, arrowX, (int) Math.round(arrowY - scrollY));
					}
					for (PlacedSlot placed : slots) {
						IRecipeSlotDrawable slot = placed.slot();
						slot.draw(guiGraphics, slot.isMouseOver(mouseX, mouseY));
					}
				} finally {
					guiGraphics.disableScissor();
				}

				// 滚动条画在裁剪框外，否则页面滚下去会把滑块一起裁掉
				drawScrollbar(guiGraphics);
			}

			@Override
			public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
				if (mouseX < 0 || mouseY < 0 || mouseX >= WIDTH || mouseY >= HEIGHT)
					return Optional.empty();
				for (PlacedSlot placed : slots) {
					// 滚出视口的槽位不该再被命中，否则会把悬停从看得见的槽位手里抢走
					int y = (int) Math.round(placed.y() - scrollY);
					if (y + SLOT_SIZE <= 0 || y >= HEIGHT)
						continue;
					IRecipeSlotDrawable slot = placed.slot();
					if (slot.isMouseOver(mouseX, mouseY))
						return Optional.of(new RecipeSlotUnderMouse(slot, getPosition()));
				}
				return Optional.empty();
			}

			@Override
			public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX,
					double scrollDeltaY) {
				int hidden = getHiddenAmount();
				if (hidden <= 0)
					return false;   // 内容装得下，把滚轮让回给 JEI
				this.scrollY = Mth.clamp(scrollY - scrollDeltaY * SCROLL_STEP, 0, hidden);
				return true;
			}

			@Override
			public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
				InputConstants.Key key = input.getKey();
				if (key.getType() != InputConstants.Type.MOUSE
						|| key.getValue() != InputConstants.MOUSE_BUTTON_LEFT)
					return false;

				if (!input.isSimulate()) {
					// 松开鼠标结束拖动，不管松在哪
					if (dragOffsetY < 0)
						return false;
					dragOffsetY = -1;
					return true;
				}

				if (getHiddenAmount() <= 0 || mouseX < getScrollbarX()
						|| mouseX >= getScrollbarX() + SCROLLBAR_WIDTH)
					return false;   // 只认滚动条那一竖条，别把槽位点击抢走

				// 滑块坐标以轨道顶边为 0（getMarkerY 那一套），鼠标先减掉那 1 像素的内缩
				int markerHeight = getMarkerHeight();
				double trackY = mouseY - SCROLLBAR_INSET;
				int markerY = getMarkerY(markerHeight);
				if (trackY < markerY || trackY >= markerY + markerHeight) {
					// 点在滑块外 → 先把滑块挪到鼠标处，再按"鼠标贴着滑块"开始拖
					moveScrollbarTo(trackY - markerHeight / 2.0, markerHeight);
					markerY = getMarkerY(markerHeight);
				}
				this.dragOffsetY = trackY - markerY;
				return true;
			}

			@Override
			public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key mouseKey,
					double dragX, double dragY) {
				if (dragOffsetY < 0 || mouseKey.getType() != InputConstants.Type.MOUSE
						|| mouseKey.getValue() != InputConstants.MOUSE_BUTTON_LEFT)
					return false;
				moveScrollbarTo(mouseY - SCROLLBAR_INSET - dragOffsetY, getMarkerHeight());
				return true;
			}

			/** 把滑块顶边挪到 markerTop（以轨道顶边为 0），换算成滚动距离 */
			private void moveScrollbarTo(double markerTop, int markerHeight) {
				int span = getMarkerSpan(markerHeight);
				if (span <= 0)
					return;
				this.scrollY = Mth.clamp(markerTop / span, 0.0, 1.0) * getHiddenAmount();
			}

			/** 滚动条轨道左边缘（右边缘正好是页面右边界，与 JEI 一样） */
			private static int getScrollbarX() {
				return WIDTH - SCROLLBAR_WIDTH;
			}

			/** 内容超出视口的部分，装得下就是 0 */
			private int getHiddenAmount() {
				return Math.max(contentHeight - HEIGHT, 0);
			}

			/** 滑块能挪动的距离：轨道扣掉上下内缩，再扣掉滑块自身的高度 */
			private static int getMarkerSpan(int markerHeight) {
				return HEIGHT - 2 * SCROLLBAR_INSET - markerHeight;
			}

			/**
			 * 滑块高度，按"看得见的部分占多少"算，下限 JEI 那个 14。
			 *
			 * 内容装得下时比例算出来大于 1，正好占满整条轨道 —— JEI 的滑块任何时候都在，
			 * 只是这时候它填满轨道、挪不动（那边也是这么算的：visible/(visible+hidden)）。
			 */
			private int getMarkerHeight() {
				int track = HEIGHT - 2 * SCROLLBAR_INSET;
				int height = Math.round(track * (HEIGHT / (float) contentHeight));
				return Mth.clamp(height, MIN_SCROLLBAR_MARKER, track);
			}

			/** 滑块顶边；0 是轨道顶边，真正落位还要再加那 1 像素的内缩 */
			private int getMarkerY(int markerHeight) {
				int hidden = getHiddenAmount();
				int span = getMarkerSpan(markerHeight);
				if (hidden <= 0 || span <= 0)
					return 0;
				return (int) Math.round(span * (scrollY / hidden));
			}

			/**
			 * 滚动条：14 宽的轨道 + 左右各内缩 1 像素的滑块，两张贴图都是 JEI 自己的。
			 *
			 * 滑块无条件画（内容装得下时它占满整条轨道），与 JEI 的 AbstractScrollWidget 一致 ——
			 * 那边 drawWidget 里背景和滑块都不做判断。
			 */
			private void drawScrollbar(GuiGraphics guiGraphics) {
				int x = getScrollbarX();
				if (scrollbarBackground != null) {
					scrollbarBackground.draw(guiGraphics, x, 0, SCROLLBAR_WIDTH, HEIGHT);
				} else {
					guiGraphics.fill(x, 0, x + SCROLLBAR_WIDTH, HEIGHT, SCROLLBAR_TRACK_COLOR);
				}

				int markerHeight = getMarkerHeight();
				int markerY = SCROLLBAR_INSET + getMarkerY(markerHeight);
				if (scrollbarMarker != null) {
					scrollbarMarker.draw(guiGraphics, x + SCROLLBAR_INSET, markerY,
							SCROLLBAR_WIDTH - 2 * SCROLLBAR_INSET, markerHeight);
				} else {
					guiGraphics.fill(x + SCROLLBAR_INSET, markerY, x + SCROLLBAR_WIDTH - SCROLLBAR_INSET,
							markerY + markerHeight, SCROLLBAR_MARKER_COLOR);
				}
			}

			/** 一行：cell 依次横向排列，y 是内容坐标 */
			private record Row(int y, List<Cell> cells, int height) {

				static Row text(int y, Cell cell) {
					return new Row(y, List.of(cell), TEXT_ROW_HEIGHT);
				}

				static Row of(int y, List<Cell> cells, int height) {
					return new Row(y, cells, height);
				}

				void draw(GuiGraphics guiGraphics, Font font, double offsetY) {
					int rowY = (int) Math.round(y - offsetY);
					int x = 0;
					for (Cell cell : cells) {
						cell.draw(guiGraphics, font, x, rowY, height);
						x += cell.width();
					}
				}
			}

			/** 一个绘制单元；width() 是占位宽度，含与后一个单元之间的间隔 */
			private interface Cell {
				int width();

				void draw(GuiGraphics guiGraphics, Font font, int x, int y, int rowHeight);
			}

			/** 占位，把后面的内容顶到行内指定位置（如右对齐的"任选 N 种"） */
			private record SpacerCell(int width) implements Cell {
				@Override
				public void draw(GuiGraphics guiGraphics, Font font, int x, int y, int rowHeight) {
				}
			}

			/** 一段文字，在行内垂直居中 */
			private record TextCell(FormattedCharSequence text, int width) implements Cell {
				@Override
				public void draw(GuiGraphics guiGraphics, Font font, int x, int y, int rowHeight) {
					guiGraphics.drawString(font, text, x, y + (rowHeight - font.lineHeight) / 2,
							FALLBACK_TEXT_COLOR, false);
				}
			}

			/** 科技图标，带槽位底图（与表头、页首物品槽同款） */
			private record IconCell(ItemStack icon, IDrawable slot) implements Cell {
				@Override
				public int width() {
					return ICON_ADVANCE;
				}

				@Override
				public void draw(GuiGraphics guiGraphics, Font font, int x, int y, int rowHeight) {
					slot.draw(guiGraphics, x, y);
					guiGraphics.renderItem(icon, x + HEADER_ICON_OFFSET, y + HEADER_ICON_OFFSET);
				}
			}

			/** 按当前语言取视觉顺序并量宽（与 JEI 渲染文本时走的同一套） */
			private static TextCell textCell(FormattedText text, Font font) {
				FormattedCharSequence sequence = Language.getInstance().getVisualOrder(text);
				return new TextCell(sequence, font.width(sequence));
			}
		}
	}
}
