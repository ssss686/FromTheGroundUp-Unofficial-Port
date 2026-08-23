package com.Fuxingcheng.ftgumod.packet.server;

import com.Fuxingcheng.ftgumod.Content;
import com.Fuxingcheng.ftgumod.FTGUConfig;
import com.Fuxingcheng.ftgumod.api.util.IStackUtils;
import com.Fuxingcheng.ftgumod.technology.Technology;
import com.Fuxingcheng.ftgumod.technology.TechnologyManager;
import com.Fuxingcheng.ftgumod.util.StackUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class CopyTechMessage {

	private final String id;

	public CopyTechMessage(String id) {
		this.id = id;
	}

	public CopyTechMessage(Technology technology) {
		this(technology.getRegistryName().toString());
	}

	public static void encode(CopyTechMessage msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.id);
	}

	public static CopyTechMessage decode(FriendlyByteBuf buf) {
		return new CopyTechMessage(buf.readUtf());
	}

	public static void handle(CopyTechMessage message, CustomPayloadEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.getSender();
			if (player == null)
				return;

			if (FTGUConfig.cachedAllowResearchCopy) {
				Technology tech = TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(message.id));

				if (tech != null && tech.canCopy() && tech.isResearched(player)) {
					int index = -1;
					for (int i = 0; i < player.getInventory().getContainerSize(); i++)
						if (!player.getInventory().getItem(i).isEmpty()
								&& player.getInventory().getItem(i).getItem() == Content.i_parchmentEmpty.get())
							index = i;

					if (index != -1) {
						player.getInventory().getItem(index).shrink(1);

						ItemStack result = StackUtils.INSTANCE.getParchment(tech, IStackUtils.Parchment.RESEARCH);
						if (player.getInventory().getFreeSlot() == -1)
							player.drop(result, true);
						else
							player.getInventory().add(result);
					}
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
