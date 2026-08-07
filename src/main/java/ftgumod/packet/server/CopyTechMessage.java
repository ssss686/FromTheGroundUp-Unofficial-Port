package ftgumod.packet.server;

import ftgumod.Content;
import ftgumod.FTGU;
import ftgumod.FTGUConfig;
import ftgumod.api.util.IStackUtils;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.util.StackUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CopyTechMessage(String id) implements CustomPacketPayload {

	public static final Type<CopyTechMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "copy_tech"));

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

	public static void handle(CopyTechMessage message, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (FTGUConfig.cachedAllowResearchCopy) {
				Technology tech = TechnologyManager.INSTANCE.getTechnology(ResourceLocation.parse(message.id()));

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
	}

}
