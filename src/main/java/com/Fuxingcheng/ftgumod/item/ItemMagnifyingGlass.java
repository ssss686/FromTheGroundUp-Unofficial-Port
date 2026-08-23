package com.Fuxingcheng.ftgumod.item;

import java.util.LinkedList;
import java.util.List;

import com.Fuxingcheng.ftgumod.Content;
import com.Fuxingcheng.ftgumod.api.util.BlockSerializable;
import com.Fuxingcheng.ftgumod.event.PlayerInspectEvent;
import com.Fuxingcheng.ftgumod.technology.Technology;
import com.Fuxingcheng.ftgumod.technology.TechnologyManager;
import com.Fuxingcheng.ftgumod.util.StackUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.MinecraftForge;

public class ItemMagnifyingGlass extends Item {

	public ItemMagnifyingGlass(Properties properties) {
		super(properties);
	}

	public static List<BlockSerializable> getInspected(ItemStack item) {
		List<BlockSerializable> list = new LinkedList<>();
		ListTag blocks = StackUtils.INSTANCE.getItemData(item).getList("FTGU", Tag.TAG_COMPOUND);
		for (int i = 0; i < blocks.size(); i++)
			list.add(new BlockSerializable(blocks.getCompound(i)));
		return list;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (player != null && player.isShiftKeyDown()) {
			if (!world.isClientSide) {
				ItemStack item = context.getItemInHand();
				List<BlockSerializable> list = getInspected(item);

				BlockState state = world.getBlockState(pos);
				ItemStack pick = state.getBlock().getCloneItemStack(world, pos, state);

				BlockSerializable block = new BlockSerializable(world, pos, state, pick);

				PlayerInspectEvent event = new PlayerInspectEvent(player, context.getHand(), pos, state, context.getClickedFace());
				event.setCanceled(true);

				for (Technology tech : TechnologyManager.INSTANCE) {
					if (tech.hasResearchRecipe()) {
						boolean canR = tech.canResearch(player);
						boolean insp = canR && tech.getResearchRecipe().inspect(block, list);
						if (insp) {
							event.setCanceled(false);
							break;
						}
					}
				}

				MinecraftForge.EVENT_BUS.post(event);
				Content.c_inspect.get().trigger((ServerPlayer) player, pos, state, !event.isCanceled());

				if (event.isCanceled()) {
					player.sendSystemMessage(Component.translatable("technology.decipher.understand"));
					SoundType sound = state.getSoundType();
					world.playSound(null, pos, sound.getHitSound(), SoundSource.NEUTRAL,
							(sound.getVolume() + 1.0F) / 4.0F, sound.getPitch() * 0.5F);
				} else {
					player.sendSystemMessage(Component.translatable("technology.decipher.flawless"));
					world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
							SoundSource.PLAYERS, 0.75F, 1.0F);

					item.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
						CompoundTag tag = data.copyTag();
						ListTag nbt = tag.getList("FTGU", Tag.TAG_COMPOUND);
						nbt.add(block.serialize());
						tag.put("FTGU", nbt);
						return CustomData.of(tag);
					});
				}
			}
			return InteractionResult.SUCCESS;
		} else
			return InteractionResult.PASS;
	}
}