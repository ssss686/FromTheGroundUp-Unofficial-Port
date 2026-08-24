package com.fuxingcheng.fromthegroundup.item;

import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.packet.PacketDispatcher;
import com.fuxingcheng.fromthegroundup.packet.client.TechnologyMessage;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.util.StackUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class ItemParchmentResearch extends Item {

	public ItemParchmentResearch(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		return InteractionResultHolder.success(research(stack, player, true));
	}

	public ItemStack research(ItemStack item, Player player, boolean already) {
		if (!player.level().isClientSide) {
			Technology t = StackUtils.INSTANCE.getTechnology(item);
			if (t != null) {
				if (t.isResearched(player)) {
					if (already)
						player.sendSystemMessage(Component.translatable("technology.complete.already", t.getDisplayText()));
				} else {
					if (t.canResearchIgnoreCustomUnlock(player)) {
						t.setResearched(player, true);

						PacketDispatcher.sendTo(new TechnologyMessage(player, true, t), (ServerPlayer) player);
						return new ItemStack(Content.i_parchmentEmpty);
					} else
						player.sendSystemMessage(Component.translatable("technology.complete.understand"));
				}
			}
		}
		return item;
	}

}
