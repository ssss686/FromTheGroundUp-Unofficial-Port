package com.Fuxingcheng.ftgumod.item;

import com.Fuxingcheng.ftgumod.packet.PacketDispatcher;
import com.Fuxingcheng.ftgumod.ClientHooks;
import com.Fuxingcheng.ftgumod.packet.client.TechnologyMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class ItemResearchBook extends Item {

	public ItemResearchBook(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		if (world.isClientSide)
			ClientHooks.openResearchBook.accept(player);
		else
			PacketDispatcher.sendTo(new TechnologyMessage(player, false), (ServerPlayer) player);
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}

}
