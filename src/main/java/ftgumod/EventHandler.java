package ftgumod;

import java.util.List;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.StartRidingTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.platform.InputConstants;
import ftgumod.api.util.BlockSerializable;
import ftgumod.event.PlayerLockEvent;
import ftgumod.item.ItemMagnifyingGlass;
import ftgumod.item.ItemParchmentResearch;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyInfoMessage;
import ftgumod.client.FTGUClient;
import ftgumod.client.gui.GuiResearchBook;
import ftgumod.packet.server.RequestMessage;
import ftgumod.technology.CapabilityTechnology;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.util.RecipeHideHelper;
import ftgumod.util.StackUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.glfw.GLFW;

public class EventHandler {

	private ItemStack stack = ItemStack.EMPTY;

	/** 锻造台箭头上那块 28×21 的报错位，数值抄自 SmithingScreen 的 ERROR_ICON_* */
	private static final int SMITHING_ERROR_X = 65;
	private static final int SMITHING_ERROR_Y = 46;
	private static final int SMITHING_ERROR_WIDTH = 28;
	private static final int SMITHING_ERROR_HEIGHT = 21;

	/** 锻造台底图，SmithingScreen 构造时传给父类的就是这一张 */
	private static final ResourceLocation SMITHING_TEXTURE = ResourceLocation
			.withDefaultNamespace("textures/gui/container/smithing.png");

	/** 本帧结果被锁定挡下来、于是被原版当成"配方无效"的锻造台；其它情况是 null，见 {@link #onPlayerInGui} */
	private SmithingScreen lockedSmithingScreen;

	/** 本帧那条报错提示还没被拦掉，拦过一次就收手，见 {@link #onRenderTooltip} */
	private boolean lockedSmithingTooltip;

	/** 上一 tick 骑上坐骑的玩家，延后一 tick 再判 started_riding，见 {@link #checkRideCriteria()} */
	private final Set<ServerPlayer> pendingRideCheck = new HashSet<>();

	/** 本 tick 对着方块用了物品的交互，延后一 tick 再判 item_used_on_block，见 {@link #checkUseOnBlockCriteria()} */
	private final List<PendingUseOnBlock> pendingUseOnBlock = new ArrayList<>();

	private record PendingUseOnBlock(ServerLevel level, ServerPlayer player, BlockPos pos, InteractionHand hand,
			ItemStack tool, BlockState preState) {
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onItemTooltip(ItemTooltipEvent evt) {
		Item item = evt.getItemStack().getItem();
		if (item == Content.i_magnifyingGlass.get()) {
			List<BlockSerializable> blocks = ItemMagnifyingGlass.getInspected(evt.getItemStack());
			long window = Minecraft.getInstance().getWindow().getWindow();
			if (InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT)
					|| InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)) {
				for (BlockSerializable block : blocks)
					evt.getToolTip().add(Component.literal(block.getLocalizedName())
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
				if (blocks.size() > 0)
					evt.getToolTip().add(Component.empty());
			} else if (blocks.size() > 0) {
				evt.getToolTip()
						.add(Component.translatable(Content.i_magnifyingGlass.get().getDescriptionId() + ".shift"));
				evt.getToolTip().add(Component.empty());
			}

			evt.getToolTip().add(Component.translatable("technology.decipher.tooltip")
					.withStyle(ChatFormatting.DARK_RED));
		} else if (item == Content.i_parchmentIdea.get()) {
			Technology tech = StackUtils.INSTANCE.getTechnology(evt.getItemStack());
			if (tech != null) {
				boolean hide = !tech.isResearched(evt.getEntity())
						&& !tech.canResearchIgnoreCustomUnlock(evt.getEntity());
				evt.getToolTip().add(Component.translatable("technology.idea",
						tech.getDisplayInfo().getTitle().getString()).withStyle(ChatFormatting.GOLD));
				evt.getToolTip().add(Component.literal(tech.getDisplayInfo().getDescription().getString())
						.withStyle(hide ? new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC,
								ChatFormatting.OBFUSCATED }
								: new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC }));
			}
		} else if (item == Content.i_parchmentResearch.get()) {
			Technology tech = StackUtils.INSTANCE.getTechnology(evt.getItemStack());
			if (tech != null) {
				boolean can = tech.isResearched(evt.getEntity())
						|| tech.canResearchIgnoreCustomUnlock(evt.getEntity());

				evt.getToolTip().add(Component.literal(tech.getDisplayInfo().getTitle().getString())
						.withStyle(ChatFormatting.GOLD));
				evt.getToolTip().add(Component.literal(tech.getDisplayInfo().getDescription().getString())
						.withStyle(can ? new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC }
								: new ChatFormatting[] { ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC,
										ChatFormatting.OBFUSCATED }));

				if (can && !tech.isResearched(evt.getEntity())) {
					evt.getToolTip().add(Component.empty());
					evt.getToolTip().add(Component.translatable("item.ftgumod.parchment_research.complete")
							.withStyle(ChatFormatting.DARK_RED));
				}
			}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onKey(InputEvent.Key evt) {
		if (FTGUClient.KEY_RESEARCH_BOOK.isDown()) {
			Minecraft.getInstance().setScreen(new GuiResearchBook(Minecraft.getInstance().player));
			PacketDispatcher.sendToServer(new RequestMessage());
		}
	}

	@SubscribeEvent
	public void onItemCraft(PlayerEvent.ItemCraftedEvent evt) {
		if (evt.getCrafting().getItem() == Content.i_researchBook.get())
			for (int i = 0; i < evt.getInventory().getContainerSize(); i++) {
				ItemStack item = evt.getInventory().getItem(i);
				if (!item.isEmpty() && item.getItem() == Content.i_parchmentResearch.get())
					((ItemParchmentResearch) item.getItem()).research(item, evt.getEntity(), false);
			}
	}

	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
		if (!evt.getEntity().level().isClientSide()) {
			ServerPlayer player = (ServerPlayer) evt.getEntity();

			AbstractContainerMenu menu = player.containerMenu;
			menu.addSlotListener(new CraftingListener(player));

			CapabilityTechnology.ITechnology cap = player.getData(CapabilityTechnology.TECH_CAP.get());
			for (Technology tech : TechnologyManager.INSTANCE.getStart()) {
				if (!cap.isResearched(tech.getRegistryName().toString())) {
					cap.setResearched(tech.getRegistryName().toString());
					tech.addRecipes(player);
				}
			}

			// 补发成就触发：已研究完成的科技不会再走一遍 setResearched，老存档登录时补上
			for (Technology tech : TechnologyManager.INSTANCE)
				if (cap.isResearched(tech.getRegistryName().toString()))
					Content.c_technologyResearched.get().trigger(player, tech);

			if (cap.isNew()) {
				if (FTGUConfig.cachedGiveResearchBook) {
					player.getInventory().add(new ItemStack(Content.i_researchBook.get()));
				}
				cap.setOld();
			}

			for (Technology tech : TechnologyManager.INSTANCE) {
				boolean hasCU = tech.hasCustomUnlock();
				boolean canRI = tech.canResearchIgnoreCustomUnlock(player);
				if (hasCU && canRI)
					tech.registerListeners(player);
			}

			PacketDispatcher.sendTo(new TechnologyInfoMessage(TechnologyManager.INSTANCE.cache), player);

			// 清理配方书中锁定的配方
			RecipeHideHelper.cleanRecipeBook(player);
		}
	}

	@SubscribeEvent
	public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent evt) {
		TechnologyManager.INSTANCE.unloadProgress(evt.getEntity());
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		if (!evt.getEntity().level().isClientSide()) {
			ServerPlayer player = (ServerPlayer) evt.getEntity();

			AbstractContainerMenu menu = player.containerMenu;
			menu.addSlotListener(new CraftingListener(player));
		}
	}

	@SubscribeEvent
	public void onPlayerOpenContainer(PlayerContainerEvent.Open evt) {
		if (!evt.getEntity().level().isClientSide()) {
			evt.getContainer().addSlotListener(new CraftingListener(evt.getEntity()));
		}
	}

	@SubscribeEvent
	public void onPlayerCloseContainer(PlayerContainerEvent.Close evt) {
		if (!evt.getEntity().level().isClientSide()) {
			evt.getEntity().containerMenu.addSlotListener(new CraftingListener(evt.getEntity()));
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onPlayerInGui(ScreenEvent.Render.Pre evt) {
		this.lockedSmithingScreen = null;
		this.lockedSmithingTooltip = false;

		if (evt.getScreen() instanceof AbstractContainerScreen<?> screen) {
			AbstractContainerMenu menu = screen.getMenu();

			// 切石机左边那排配方不走配方书，客户端这份是本地现查的（服务端那份在自己那边摘，
			// 两边下标要对上，详见 RecipeHideHelper.filterStonecutterRecipes）
			RecipeHideHelper.filterStonecutterRecipes(menu, Minecraft.getInstance().player);

			for (Slot s : menu.slots) {
				if (s.container instanceof ResultContainer resultContainer) {
					ItemStack slotStack = s.container.getItem(0);
					if (slotStack.isEmpty())
						this.stack = slotStack;
					else if (slotStack != this.stack) {
						PlayerLockEvent event = new PlayerLockEvent(
								Minecraft.getInstance().player, slotStack,
								resultContainer.getRecipeUsed());
						NeoForge.EVENT_BUS.post(event);

						if (!event.isCanceled())
							s.container.setItem(0, ItemStack.EMPTY);
						this.stack = s.container.getItem(0);
					}
					break;
				}
			}

			// 结果刚被上面清掉，这时候问锻造台"是不是被锁下来的"才问得准
			if (screen instanceof SmithingScreen smithing && isLockedSmithingResult(smithing)) {
				this.lockedSmithingScreen = smithing;
				this.lockedSmithingTooltip = isHoveringSmithingError(smithing, evt.getMouseX(), evt.getMouseY());
			}
		}
	}

	/**
	 * 锻造台的结果槽空着，但配方其实查得到 —— 那这个空只可能是锁定挡的，
	 * 不是原版那条"该物品无法使用此方法升级"要说的意思。
	 *
	 * 原版 SmithingMenu#createResult 查得到配方就会把结果填上，填不上只剩两种情况：
	 * 配方被 {@link TechnologyManager#isLocked} 拦下，或者结果物品被特性开关禁用。
	 * 所以这里照样拿配方自己算一遍，只有结果确实锁着才算数。
	 */
	@OnlyIn(Dist.CLIENT)
	private static boolean isLockedSmithingResult(SmithingScreen screen) {
		SmithingMenu menu = screen.getMenu();
		if (!menu.getSlot(SmithingMenu.TEMPLATE_SLOT).hasItem()
				|| !menu.getSlot(SmithingMenu.BASE_SLOT).hasItem()
				|| !menu.getSlot(SmithingMenu.ADDITIONAL_SLOT).hasItem()
				|| menu.getSlot(menu.getResultSlot()).hasItem())
			return false;

		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player == null || minecraft.level == null)
			return false;

		SmithingRecipeInput input = new SmithingRecipeInput(
				menu.getSlot(SmithingMenu.TEMPLATE_SLOT).getItem(),
				menu.getSlot(SmithingMenu.BASE_SLOT).getItem(),
				menu.getSlot(SmithingMenu.ADDITIONAL_SLOT).getItem());
		for (RecipeHolder<SmithingRecipe> recipe : minecraft.level.getRecipeManager()
				.getRecipesFor(RecipeType.SMITHING, input, minecraft.level))
			if (TechnologyManager.INSTANCE
					.isLocked(recipe.value().assemble(input, minecraft.level.registryAccess()), player))
				return true;
		return false;
	}

	/** 鼠标是不是停在箭头上那块报错位，判法跟原版 AbstractContainerScreen#isHovering 一致（左右各放宽一格） */
	@OnlyIn(Dist.CLIENT)
	private static boolean isHoveringSmithingError(SmithingScreen screen, int mouseX, int mouseY) {
		int x = mouseX - screen.getGuiLeft();
		int y = mouseY - screen.getGuiTop();
		return x >= SMITHING_ERROR_X - 1 && x < SMITHING_ERROR_X + SMITHING_ERROR_WIDTH + 1
				&& y >= SMITHING_ERROR_Y - 1 && y < SMITHING_ERROR_Y + SMITHING_ERROR_HEIGHT + 1;
	}

	/**
	 * 原版判"配方有错"只看三个输入放了没、结果槽空没空，锁定挡下来的结果正好落在里面，
	 * 于是在箭头上盖一张红叉。可锁定不是"没法用这种方法升级"，这里把那一格底图重贴回来。
	 */
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onContainerBackground(ContainerScreenEvent.Render.Background evt) {
		if (evt.getContainerScreen() != this.lockedSmithingScreen)
			return;

		evt.getGuiGraphics().blit(SMITHING_TEXTURE,
				evt.getContainerScreen().getGuiLeft() + SMITHING_ERROR_X,
				evt.getContainerScreen().getGuiTop() + SMITHING_ERROR_Y,
				SMITHING_ERROR_X, SMITHING_ERROR_Y, SMITHING_ERROR_WIDTH, SMITHING_ERROR_HEIGHT);
	}

	/**
	 * 红叉上的那条提示也拦掉，只留 JEI 的"显示配方"。
	 *
	 * 拦法是在 {@link #onPlayerInGui} 里先备好"这一帧要拦一条"，拦到就把标记清掉：
	 * 原版那条在 SmithingScreen#render 里画，JEI 的"显示配方"要等 ScreenEvent.Render.Post
	 * 才画，而鼠标停在箭头上时那块报错位底下没有槽位，这一帧第一条过这里的就只有原版那条。
	 */
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onRenderTooltip(RenderTooltipEvent.Pre evt) {
		if (!this.lockedSmithingTooltip)
			return;

		this.lockedSmithingTooltip = false;
		evt.setCanceled(true);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onEntityJoinLevel(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide() && event.getEntity() == Minecraft.getInstance().player)
			PacketDispatcher.sendToServer(new RequestMessage());
	}

	@SubscribeEvent
	public void onEntityJoinLevelServer(EntityJoinLevelEvent event) {
		if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
			// 延迟一tick清理配方书，确保配方同步完成
			player.getServer().execute(() -> RecipeHideHelper.cleanRecipeBook(player));
		}
	}

	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Post event) {
		checkRideCriteria();
		checkUseOnBlockCriteria();

		for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
			var fakeMap = TechnologyManager.INSTANCE.getFakeAdvancements().get(player);
			if (fakeMap != null && !fakeMap.isEmpty()) {
				List<Pair<Technology, String>> toGrant = new ArrayList<>();
				for (var entry : fakeMap.entrySet()) {
					if (player.getAdvancements().getOrStartProgress(entry.getKey()).isDone())
						toGrant.add(entry.getValue());
				}
				for (var pair : toGrant)
					pair.getLeft().grantCriterion(player, pair.getRight());
			}
		}

		// Periodically check pending criteria for PlayerTrigger (location) and item_inventory
		if (event.getServer().getTickCount() % 20 == 0) {
			for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
				List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
				if (pending != null && !pending.isEmpty()) {
					List<TechnologyManager.PendingCriterion> matched = new ArrayList<>();
					for (TechnologyManager.PendingCriterion pc : pending) {
						if (pc.instance() instanceof PlayerTrigger.TriggerInstance pi) {
							var playerPred = pi.player();
							if (playerPred.isEmpty() || playerPred.get().matches(locationContext(player)))
								matched.add(pc);
						} else if (pc.instance() instanceof InventoryChangeTrigger.TriggerInstance ii) {
							var playerPred = ii.player();
							if (playerPred.isPresent() && !playerPred.get().matches(locationContext(player)))
								continue;
							if (matchesInventory(ii, player.getInventory()))
								matched.add(pc);
						}
					}
					for (TechnologyManager.PendingCriterion pc : matched)
						pc.tech().grantCriterion(player, pc.criterionName());
				}
			}

			if (ftgumod.criterion.TriggerItemInventory.INSTANCE != null) {
				for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
					ftgumod.criterion.TriggerItemInventory.INSTANCE.trigger(player);
				}
			}
		}
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntity().level().isClientSide())
			return;

		Entity killed = event.getEntity();
		Entity killer = event.getSource().getEntity();
		if (!(killer instanceof ServerPlayer player))
			return;

		List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
		if (pending == null || pending.isEmpty())
			return;

		List<TechnologyManager.PendingCriterion> matched = new ArrayList<>();
		for (TechnologyManager.PendingCriterion pc : pending) {
			if (pc.instance() instanceof KilledTrigger.TriggerInstance ki) {
				var entityPred = ki.entityPredicate();
				if (entityPred.isPresent()) {
					LootParams lootParams = new LootParams.Builder(player.serverLevel())
						.withParameter(LootContextParams.THIS_ENTITY, killed)
						.withParameter(LootContextParams.ORIGIN, player.position())
						.withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
						.create(LootContextParamSets.ADVANCEMENT_ENTITY);
					LootContext ctx = new LootContext.Builder(lootParams).create(Optional.empty());
					if (entityPred.get().matches(ctx))
						matched.add(pc);
				} else {
					matched.add(pc);
				}
			}
		}

		for (TechnologyManager.PendingCriterion pc : matched)
			pc.tech().grantCriterion(player, pc.criterionName());
	}

	@SubscribeEvent
	public void onMobEffect(MobEffectEvent.Added event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		if (player.level().isClientSide())
			return;

		List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
		if (pending == null || pending.isEmpty())
			return;

		List<TechnologyManager.PendingCriterion> matched = new ArrayList<>();
		for (TechnologyManager.PendingCriterion pc : pending) {
			if (pc.instance() instanceof EffectsChangedTrigger.TriggerInstance ei) {
				var effects = ei.effects();
				if (effects.isEmpty() || effects.get().matches(player))
					matched.add(pc);
			}
		}

		for (TechnologyManager.PendingCriterion pc : matched)
			pc.tech().grantCriterion(player, pc.criterionName());
	}

	@SubscribeEvent
	public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player))
			return;
		if (!(event.getLevel() instanceof ServerLevel level))
			return;

		List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
		if (pending == null || pending.isEmpty())
			return;

		boolean relevant = false;
		for (TechnologyManager.PendingCriterion pc : pending)
			if (pc.instance() instanceof ItemUsedOnLocationTrigger.TriggerInstance) {
				relevant = true;
				break;
			}
		if (!relevant)
			return;

		BlockPos pos = event.getPos();
		pendingUseOnBlock.add(new PendingUseOnBlock(level, player, pos.immutable(), event.getHand(),
				event.getItemStack().copy(), level.getBlockState(pos)));
	}

	@SubscribeEvent
	public void onEntityMount(EntityMountEvent event) {
		if (event.getLevel().isClientSide() || !event.isMounting())
			return;
		if (event.getEntityMounting() instanceof ServerPlayer player)
			pendingRideCheck.add(player);
	}

	/**
	 * 判定 started_riding 条件。
	 *
	 * EntityMountEvent 由 Entity#startRiding 里的 canMountEntity 抛出，位置在 this.vehicle = vehicle
	 * 之前 —— 事件触发那一刻 player.getVehicle() 还是空的，vehicle 谓词必然不成立。原版的
	 * START_RIDING_TRIGGER 是在 vehicle / addPassenger 都设好之后才抛的，所以这里延后一 tick 再判，
	 * 语义和原版 SimpleCriterionTrigger.trigger 对齐（玩家谓词同样是拿 createContext(player, player) 判）。
	 *
	 * 不能图省事用 player.server.execute(...)：BlockableEventLoop#execute 在服务器线程上调用时
	 * scheduleExecutables() 返回 false，会当场 inline 执行，等于没延后。
	 */
	private void checkRideCriteria() {
		if (pendingRideCheck.isEmpty())
			return;

		for (ServerPlayer player : pendingRideCheck) {
			List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
			if (pending == null || pending.isEmpty())
				continue;

			LootContext ctx = EntityPredicate.createContext(player, player);
			List<TechnologyManager.PendingCriterion> matched = new ArrayList<>();
			for (TechnologyManager.PendingCriterion pc : pending) {
				if (pc.instance() instanceof StartRidingTrigger.TriggerInstance si) {
					var playerPred = si.player();
					if (playerPred.isEmpty() || playerPred.get().matches(ctx))
						matched.add(pc);
				}
			}

			for (TechnologyManager.PendingCriterion pc : matched)
				pc.tech().grantCriterion(player, pc.criterionName());
		}

		pendingRideCheck.clear();
	}

	/**
	 * 判定 item_used_on_block 条件。
	 *
	 * 原版的 ITEM_USED_ON_BLOCK 是 ServerPlayerGameMode#useItemOn 在方块交互成功
	 * （InteractionResult.consumesAction）之后抛的，那一刻读到的方块状态已经是交互之后的
	 * —— 蜂箱的 honey_level 被重置成 0 了 —— 而手里的物品是交互前的 stack.copy()。
	 * NeoForge 的 RightClickBlock 在交互之前抛，既拿不到返回值也没有"交互完成"事件，
	 * 所以这里先把交互记下来，下一 tick 再看方块状态或手上的物品有没有变化来反推交互确实生效
	 * （空蜂箱上用瓶子：两者都没变，不算数）。
	 *
	 * 判据的 LootContext 与原版 ItemUsedOnLocationTrigger#trigger 一致（ORIGIN 取方块中心、
	 * BLOCK_STATE 取交互后的状态、TOOL 取交互前的物品），玩家谓词照原版用
	 * EntityPredicate.createContext；所以原版 safely_harvest_honey 那种 predicate 可以原样抄。
	 *
	 * 已知差异：交互成功但既不改方块状态也不改物品的（例如开箱子这类只弹界面）不会触发。
	 */
	private void checkUseOnBlockCriteria() {
		if (pendingUseOnBlock.isEmpty())
			return;

		for (PendingUseOnBlock use : pendingUseOnBlock) {
			ServerPlayer player = use.player();
			if (player.isRemoved())
				continue;

			BlockState state = use.level().getBlockState(use.pos());
			ItemStack held = player.getItemInHand(use.hand());
			if (state == use.preState() && held.getItem() == use.tool().getItem()
					&& held.getDamageValue() == use.tool().getDamageValue())
				continue;

			List<TechnologyManager.PendingCriterion> pending = TechnologyManager.INSTANCE.getPendingCriteria().get(player);
			if (pending == null || pending.isEmpty())
				continue;

			LootParams lootParams = new LootParams.Builder(use.level())
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(use.pos()))
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.withParameter(LootContextParams.BLOCK_STATE, state)
				.withParameter(LootContextParams.TOOL, use.tool())
				.create(LootContextParamSets.ADVANCEMENT_LOCATION);
			LootContext ctx = new LootContext.Builder(lootParams).create(Optional.empty());
			LootContext playerCtx = EntityPredicate.createContext(player, player);

			List<TechnologyManager.PendingCriterion> matched = new ArrayList<>();
			for (TechnologyManager.PendingCriterion pc : pending) {
				if (pc.instance() instanceof ItemUsedOnLocationTrigger.TriggerInstance ui) {
					var playerPred = ui.player();
					if (playerPred.isPresent() && !playerPred.get().matches(playerCtx))
						continue;
					var locationPred = ui.location();
					if (locationPred.isPresent() && !locationPred.get().matches(ctx))
						continue;
					matched.add(pc);
				}
			}

			for (TechnologyManager.PendingCriterion pc : matched)
				pc.tech().grantCriterion(player, pc.criterionName());
		}

		pendingUseOnBlock.clear();
	}

	/**
	 * 玩家所在地点的 LootContext。ADVANCEMENT_LOCATION 参数集要的四个参数都填满
	 * （THIS_ENTITY / ORIGIN / BLOCK_STATE / TOOL），location 与 inventory_changed
	 * 的玩家谓词都拿它来判，跟原版 LocationTrigger#trigger 一致。
	 */
	private static LootContext locationContext(ServerPlayer player) {
		LootParams lootParams = new LootParams.Builder(player.serverLevel())
			.withParameter(LootContextParams.THIS_ENTITY, player)
			.withParameter(LootContextParams.ORIGIN, player.position())
			.withParameter(LootContextParams.BLOCK_STATE, player.getBlockStateOn())
			.withParameter(LootContextParams.TOOL, player.getMainHandItem())
			.create(LootContextParamSets.ADVANCEMENT_LOCATION);
		return new LootContext.Builder(lootParams).create(Optional.empty());
	}

	/**
	 * 判定 inventory_changed 的 items / slots 条件。
	 *
	 * 原版是在 Inventory 变动时把"刚变动的那一格"交给 TriggerInstance#matches 判的，
	 * 这里没有那个时刻，所以照原版 trigger 的做法把整包装填状态统计出来，再逐格拿非空格
	 * 去问 matches —— items 只有一项时它判的就是传进去的这格，语义正好落在
	 * "背包里存在一格满足谓词"上；items 多于一项时它自己会扫全包，重复问结果也一样。
	 */
	private static boolean matchesInventory(InventoryChangeTrigger.TriggerInstance instance, Inventory inventory) {
		int full = 0, empty = 0, occupied = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty()) {
				empty++;
			} else {
				occupied++;
				if (stack.getCount() >= stack.getMaxStackSize())
					full++;
			}
		}

		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty() && instance.matches(inventory, stack, full, empty, occupied))
				return true;
		}
		return false;
	}

	// JEI research guide - no longer needs tick refresh

}
