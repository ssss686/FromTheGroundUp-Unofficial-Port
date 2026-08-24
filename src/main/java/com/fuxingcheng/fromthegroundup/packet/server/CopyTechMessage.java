package com.fuxingcheng.fromthegroundup.packet.server;

import com.fuxingcheng.fromthegroundup.Content;
import com.fuxingcheng.fromthegroundup.FromTheGroundUp;
import com.fuxingcheng.fromthegroundup.FTGUConfig;
import com.fuxingcheng.fromthegroundup.api.util.IStackUtils;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import com.fuxingcheng.fromthegroundup.util.StackUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record CopyTechMessage(String id) implements CustomPacketPayload {

	public static final Type<CopyTechMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "copy_tech"));

	public static final StreamCodec<FriendlyByteBuf, CopyTechMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, CopyTechMessage::id,
			CopyTechMessage::new
	);

	public CopyTechMessage(Technology technology) {
		this(technology.getRegistryName().toString());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(CopyTechMessage message, ServerPlayer player) {
		if (FTGUConfig.cachedAllowResearchCopy) {
			Technology tech = TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(message.id()));

			if (tech != null && tech.canCopy() && tech.isResearched(player)) {
				int index = -1;
				for (int i = 0; i < player.getInventory().getContainerSize(); i++)
					if (!player.getInventory().getItem(i).isEmpty()
							&& player.getInventory().getItem(i).getItem() == Content.i_parchmentEmpty)
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
	}

}
