package com.fuxingcheng.fromthegroundup.technology;

import java.util.Collection;
import java.util.HashSet;

import com.fuxingcheng.fromthegroundup.FromTheGroundUp;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public class CapabilityTechnology {

	public static final AttachmentType<ITechnology> TECH_CAP = AttachmentRegistry.create(
			ResourceLocation.fromNamespaceAndPath(FromTheGroundUp.MODID, "technology"),
			builder -> builder
					.initializer(DefaultImpl::new)
					.persistent(ITechnology.CODEC)
					.copyOnDeath()
	);

	public static void register() {
		// Player clone event - copy technology data on death
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			if (!alive) {
				ITechnology cap1 = oldPlayer.getAttachedOrCreate(TECH_CAP);
				ITechnology cap2 = newPlayer.getAttachedOrCreate(TECH_CAP);
				cap2.setResearched(cap1.getResearched());
				if (!cap1.isNew())
					cap2.setOld();
			}
		});
	}

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

		net.minecraft.util.codecs.Codec<ITechnology> CODEC = new net.minecraft.util.codecs.Codec<>() {
			@Override
			public <T> com.mojang.datafixers.util.Either<ITechnology, T> decode(com.mojang.serialization.DynamicOps<T> ops, T input) {
				if (input instanceof CompoundTag tag) {
					DefaultImpl impl = new DefaultImpl();
					impl.load(tag);
					return com.mojang.datafixers.util.Either.left(impl);
				}
				return com.mojang.datafixers.util.Either.right(input);
			}

			@Override
			public <T> com.mojang.datafixers.util.Either<T, com.mojang.serialization.DataResult.Error<ITechnology>> encode(ITechnology input, com.mojang.serialization.DynamicOps<T> ops, T prefix) {
				return com.mojang.datafixers.util.Either.left(ops.createString(input.write().toString()));
			}
		};
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
