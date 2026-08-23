package com.Fuxingcheng.ftgumod.technology;

import java.util.Collection;
import java.util.HashSet;
import javax.annotation.Nullable;

import com.Fuxingcheng.ftgumod.FTGU;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityTechnology {

	// 1. 定义能力令牌（Forge 1.21.1 推荐方式）
	public static final Capability<ITechnology> TECH_CAP = CapabilityManager.get(new CapabilityToken<>() {});

	// 2. 能力提供者（用于挂载到玩家实体）
	public static class Provider implements ICapabilityProvider, net.minecraftforge.common.util.INBTSerializable<CompoundTag> {
		private final DefaultImpl impl = new DefaultImpl();
		private final LazyOptional<ITechnology> holder = LazyOptional.of(() -> impl);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
			return cap == TECH_CAP ? holder.cast() : LazyOptional.empty();
		}

		@Override
		public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider registryAccess) {
			return impl.write();
		}

		@Override
		public void deserializeNBT(net.minecraft.core.HolderLookup.Provider registryAccess, CompoundTag nbt) {
			impl.load(nbt);
		}
	}

	// 3. 事件监听（负责挂载、克隆等）
	public CapabilityTechnology() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			Provider provider = new Provider();
			event.addCapability(
					ResourceLocation.fromNamespaceAndPath(FTGU.MODID, "technology"),
					provider
			);
			// 保存 provider 以便后续克隆时复制数据（可选，通过 PlayerEvent.Clone 从旧实体复制）
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		// 当玩家重生/传送时，从旧实体复制能力数据
		Player oldPlayer = evt.getOriginal();
		Player newPlayer = evt.getEntity();

		oldPlayer.getCapability(TECH_CAP).ifPresent(oldCap -> {
			newPlayer.getCapability(TECH_CAP).ifPresent(newCap -> {
				// 复制研究列表和 isNew 标志
				newCap.setResearched(oldCap.getResearched());
				if (!oldCap.isNew())
					newCap.setOld();
			});
		});
	}

	// ========== 以下为原有的 ITechnology 接口和 DefaultImpl（保持不变） ==========
	public interface ITechnology {

		boolean isResearched(String tech);

		boolean isNew();

		void setOld();

		void setResearched(String tech);

		Collection<String> getResearched();

		void setResearched(Collection<String> tech);

		void clear();

		void removeResearched(String tech);

		default CompoundTag write() {
			CompoundTag compound = new CompoundTag();
			ListTag list = new ListTag();
			for (String s : getResearched())
				list.add(StringTag.valueOf(s));
			compound.putBoolean("new", isNew());
			compound.put("researched", list);
			return compound;
		}
	}

	public static class DefaultImpl implements ITechnology {

		private final Collection<String> tech = new HashSet<>();
		private boolean isNew = true;

		@Override
		public boolean isResearched(String tech) {
			return this.tech.contains(tech);
		}

		@Override
		public void setResearched(String tech) {
			this.tech.add(tech);
		}

		@Override
		public Collection<String> getResearched() {
			return tech;
		}

		@Override
		public void setResearched(Collection<String> tech) {
			this.tech.addAll(tech);
		}

		@Override
		public void clear() {
			tech.clear();
		}

		@Override
		public boolean isNew() {
			return isNew;
		}

		@Override
		public void setOld() {
			isNew = false;
		}

		@Override
		public void removeResearched(String tech) {
			this.tech.remove(tech);
			this.tech.removeIf(string -> string.startsWith(tech + "#"));
		}

		public void load(CompoundTag compound) {
			tech.clear();
			ListTag list = compound.getList("researched", Tag.TAG_STRING);
			for (int i = 0; i < list.size(); i++)
				tech.add(list.getString(i));
			isNew = compound.getBoolean("new");
		}
	}
}