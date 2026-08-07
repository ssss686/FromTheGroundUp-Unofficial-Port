package ftgumod.inventory;

import java.util.List;
import java.util.function.Predicate;

import ftgumod.Content;
import ftgumod.api.inventory.ContainerResearch;
import ftgumod.api.inventory.InventoryCraftingPersistent;
import ftgumod.api.inventory.SlotCrafting;
import ftgumod.api.inventory.SlotSpecial;
import ftgumod.api.util.IStackUtils;
import ftgumod.api.technology.puzzle.PuzzleConnect;
import ftgumod.api.technology.puzzle.PuzzleMatch;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.HintMessage;
import ftgumod.packet.client.TechnologyMessage;
import ftgumod.technology.Technology;
import ftgumod.tileentity.TileEntityInventory;
import ftgumod.tileentity.TileEntityResearchTable;
import ftgumod.util.StackUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;

public class ContainerResearchTable extends ContainerResearch {

	private static final TagKey<Item> FEATHERS = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "feathers"));

	public final TileEntityResearchTable invInput;

	public final Inventory invPlayer;

	private final int sizeInventory;
	private final Container result;

	public int output;
	public int glass;
	private int feather;
	private int parchment;

	public ContainerResearchTable(int containerId, TileEntityResearchTable tileEntity, Inventory invPlayer) {
		super(Content.m_researchTable.get(), containerId);
		this.invInput = tileEntity;
		this.invPlayer = invPlayer;
		if (!invPlayer.player.level().isClientSide())
			PacketDispatcher.sendTo(new TechnologyMessage(invPlayer.player, false), (ServerPlayer) invPlayer.player);

		result = new ResultContainer();
		sizeInventory = addSlots(tileEntity);

		for (int sloty = 0; sloty < 3; sloty++) {
			for (int slotx = 0; slotx < 9; slotx++) {
				addSlot(new Slot(invPlayer, slotx + sloty * 9 + 9, 8 + slotx * 18, 84 + sloty * 18));
			}
		}

		for (int slot = 0; slot < 9; slot++) {
			addSlot(new Slot(invPlayer, slot, 8 + slot * 18, 142));
		}

		if (invInput.puzzle != null) {
			invInput.puzzle.onStart(this);
			invInput.puzzle.onInventoryChange(this);
		}

		slotsChanged(null);
	}

	private int addSlots(TileEntityInventory tileEntity) {
		int c = 0;

		addSlot(new SlotCrafting(this, tileEntity, c, 8, 46, 1, stack -> stack.is(FEATHERS)));
		feather = c;
		c++;

		addSlot(new SlotCrafting(this, tileEntity, c, 8, 24, 1, new ItemStack(Content.i_parchmentIdea.get())));
		parchment = c;
		c++;

		addSlot(new SlotCrafting(this, tileEntity, c, 150, 35, 1, new ItemStack(Content.i_magnifyingGlass.get())));
		glass = c;
		c++;

		addSlot(new SlotSpecial(result, c, 124, 35, 1, i -> false));
		output = c;
		c++;

		// Pre-allocate 9 match puzzle slots (always present, tiles 3-11 of TE inventory)
		Container matchInv = new InventoryCraftingPersistent(tileEntity, 3, 3, 3);
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++) {
				final int mi = x + y * 3;
				addSlot(new SlotCrafting(this, matchInv, mi, 30 + x * 18, 17 + y * 18, 1, (Predicate<ItemStack>) null) {
					@Override
					public boolean isActive() {
						return invInput.puzzle instanceof PuzzleMatch;
					}
				});
				c++;
			}

		// Pre-allocate 3 connect puzzle slots (always present, tiles 3-5 of TE inventory)
		Container connectInv = new InventoryCraftingPersistent(tileEntity, 3, 3, 1);
		for (int i = 0; i < 3; i++) {
			final int ci = i;
			addSlot(new SlotCrafting(this, connectInv, i, 44 + i * 18, 35, 1,
					stack -> {
						if (invInput.puzzle instanceof PuzzleConnect pc)
							return pc.canPlaceInSlot(stack, ci);
						return false;
					}) {
				@Override
				public boolean isActive() {
					return invInput.puzzle instanceof PuzzleConnect;
				}
			});
			c++;
		}

		return c;
	}

	@Override
	public void slotsChanged(Container inv) {
		if (inv != invPlayer) {
			if (slots.get(parchment).hasItem()) {
				Technology tech = StackUtils.INSTANCE.getTechnology(slots.get(parchment).getItem());
				if (tech != null && tech.hasResearchRecipe()
						&& (invInput.puzzle == null || invInput.puzzle.getRecipe().getTechnology() != tech)) {
					if (invInput.puzzle != null) {
						invInput.puzzle.onRemove(invPlayer.player, invInput.getLevel(), invInput.getBlockPos());
						invPlayer.player.level().playSound(null, invInput.getBlockPos(), SoundEvents.ARMOR_EQUIP_GENERIC.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
					}
					invInput.puzzle = tech.getResearchRecipe().createInstance();
					invInput.puzzle.onStart(this);
					invInput.puzzle.onInventoryChange(this);
				}
			} else if (invInput.puzzle != null) {
				boolean hadItems = false;
				for (int i = 3; i <= 11; i++)
					if (!invInput.getItem(i).isEmpty()) {
						hadItems = true;
						break;
					}
				invInput.puzzle.onRemove(invPlayer.player, invInput.getLevel(), invInput.getBlockPos());
				invInput.puzzle = null;
				if (hadItems && invPlayer.player instanceof ServerPlayer sp)
					sp.connection.send(new ClientboundSoundPacket(SoundEvents.ARMOR_EQUIP_GENERIC, net.minecraft.sounds.SoundSource.PLAYERS, sp.getX(), sp.getY(), sp.getZ(), 1.0F, 1.0F, sp.level().getRandom().nextLong()));
			}

			if (invInput.puzzle != null) {
				if (inv != result)
					invInput.puzzle.onInventoryChange(this);
				if (slots.get(feather).hasItem()
						&& invInput.puzzle.getRecipe().getTechnology().canResearch(invPlayer.player)
						&& invInput.puzzle.test()) {
					slots.get(output).set(StackUtils.INSTANCE
							.getParchment(invInput.puzzle.getRecipe().getTechnology(), IStackUtils.Parchment.RESEARCH));
					return;
				}
			}

			slots.get(output).set(ItemStack.EMPTY);
		}
	}

	@Override
	public void clicked(int index, int mouse, ClickType mode, Player player) {
		if (mode != ClickType.CLONE && index == output && slots.get(output).hasItem()) {
			slots.get(parchment).remove(1);
			invInput.puzzle.onFinish();
			invInput.puzzle.onRemove(player, invInput.getLevel(), invInput.getBlockPos());
			invInput.puzzle = null;
			player.level().playSound(null, invInput.getBlockPos(), SoundEvents.ARMOR_EQUIP_GENERIC.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		super.clicked(index, mouse, mode, player);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int slotIndex) {
		ItemStack itemStack1 = ItemStack.EMPTY;
		Slot slot = slots.get(slotIndex);

		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack1 = itemStack2.copy();

			if (slotIndex == output) {
				if (!moveItemStackTo(itemStack2, sizeInventory, sizeInventory + 36, true))
					return ItemStack.EMPTY;
			} else if (slotIndex > output && slotIndex < sizeInventory + 36) {
				if (itemStack2.getItem() == Content.i_parchmentIdea.get()) {
					if (!moveItemStackTo(itemStack2, parchment, parchment + 1, false))
						return ItemStack.EMPTY;
				} else if (itemStack2.is(FEATHERS)) {
					if (!moveItemStackTo(itemStack2, feather, feather + 1, false))
						return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			} else if (!moveItemStackTo(itemStack2, sizeInventory, sizeInventory + 36, false))
				return ItemStack.EMPTY;

			if (itemStack2.getCount() != 0)
				slot.setChanged();

			if (itemStack2.getCount() == itemStack1.getCount())
				return ItemStack.EMPTY;

			slot.onTake(playerIn, itemStack2);
		}

		return itemStack1;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public boolean isClient() {
		return invInput.getLevel().isClientSide();
	}

	@Override
	public Player getPlayer() {
		return invPlayer.player;
	}

	@Override
	public void refreshHints(List<Component> hints) {
		if (invPlayer.player instanceof ServerPlayer sp)
			PacketDispatcher.sendTo(new HintMessage(hints), sp);
	}

	@Override
	public void setChanged() {
		invInput.setChanged();

		BlockState state = invInput.getLevel().getBlockState(invInput.getBlockPos());
		invInput.getLevel().sendBlockUpdated(invInput.getBlockPos(), state, state, 2);
	}

}
