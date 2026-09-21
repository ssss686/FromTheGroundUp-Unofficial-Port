package com.fuxingcheng.fromthegroundup;

import org.jetbrains.annotations.Nullable;

import com.fuxingcheng.fromthegroundup.api.util.IStackUtils.Parchment;
import com.fuxingcheng.fromthegroundup.technology.Technology;
import com.fuxingcheng.fromthegroundup.technology.TechnologyManager;
import com.fuxingcheng.fromthegroundup.util.StackUtils;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * 流浪商人的稀有档（等级 2 的那一条随机交易）。
 * 1.21.1 原版没给交易做数据包入口，Fabric 这边挂 TradeOfferHelper —— 它把这里的清单塞进
 * VillagerTrades.WANDERING_TRADER_TRADES 的 2 号槽，原版取值时和那批稀有交易一起随机。
 * 科技在 getOffer 里现查，不缓存 Technology 对象，所以 /reload 换了科技定义也不会拿到旧的。
 */
public class VillagerTradesHandler {

	private static final ResourceLocation ICE_HARVESTING = ResourceLocation.parse("ftgumod:construction/ice_harvesting");
	private static final int ICE_HARVESTING_PRICE = 10;

	/**
	 * 必须在 mod 初始化阶段调用一次。
	 * TradeOfferHelper 是"注册即写入"，consumer 只在注册那一刻跑一趟，加进去的 ItemListing
	 * 会一直留在原版那张表里，所以这里不能放到数据重载里反复注册（会越加越多）。
	 */
	public static void register() {
		TradeOfferHelper.registerWanderingTraderOffers(2,
				trades -> trades.add(new ParchmentTrade(ICE_HARVESTING, ICE_HARVESTING_PRICE)));
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
