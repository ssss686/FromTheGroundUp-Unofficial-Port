package ftgumod.item;

import ftgumod.client.gui.GuiResearchBook;
import ftgumod.packet.PacketDispatcher;
import ftgumod.packet.client.TechnologyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class ItemResearchBook extends Item {

	public ItemResearchBook(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		if (world.isClientSide)
			Minecraft.getInstance().setScreen(new GuiResearchBook(player));
		else
			PacketDispatcher.sendTo(new TechnologyMessage(player, false), (ServerPlayer) player);
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}

}
