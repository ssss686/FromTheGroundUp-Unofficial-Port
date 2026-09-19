package ftgumod;

import javax.annotation.Nullable;

import ftgumod.api.util.IStackUtils.Parchment;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.util.StackUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

/**
 * 流浪商人的稀有档（等级 2 的那一条随机交易）。
 * 1.21.1 原版没给交易做数据包入口，只能挂 NeoForge 的 WandererTradesEvent。
 * 科技在 getOffer 里现查，服务器每次数据重载都会重建这个列表，不会累积重复。
 */
public class VillagerTradesHandler {

	private static final ResourceLocation ICE_HARVESTING = ResourceLocation.parse("ftgumod:construction/ice_harvesting");
	private static final int ICE_HARVESTING_PRICE = 10;

	@SubscribeEvent
	public void onWandererTrades(WandererTradesEvent event) {
		event.getRareTrades().add(new ParchmentTrade(ICE_HARVESTING, ICE_HARVESTING_PRICE));
	}

	private record ParchmentTrade(ResourceLocation technology, int price) implements VillagerTrades.ItemListing {

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource random) {
			Technology tech = TechnologyManager.INSTANCE.getTechnology(technology);
			if (tech == null)
				return null;
			return new MerchantOffer(new ItemCost(Items.EMERALD, price),
					StackUtils.INSTANCE.getParchment(tech, Parchment.RESEARCH), 2, 1, 0.05F);
		}

	}

}
