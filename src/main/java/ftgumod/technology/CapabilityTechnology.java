package ftgumod.technology;

import java.util.Collection;
import java.util.HashSet;

import ftgumod.FTGU;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CapabilityTechnology {

	@SuppressWarnings("unchecked")
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
			(DeferredRegister) DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FTGU.MODID);

	public static final Supplier<AttachmentType<ITechnology>> TECH_CAP =
			ATTACHMENT_TYPES.register("technology",
					() -> AttachmentType.builder(() -> (ITechnology) new DefaultImpl())
							.serialize(new IAttachmentSerializer<CompoundTag, ITechnology>() {
								@Override
								public ITechnology read(IAttachmentHolder holder, CompoundTag tag,
										HolderLookup.Provider provider) {
									DefaultImpl impl = new DefaultImpl();
									impl.load(tag);
									return impl;
								}

								@Override
								public CompoundTag write(ITechnology attachment,
										HolderLookup.Provider provider) {
									return attachment.write();
								}
							})
							.build());

	public CapabilityTechnology() {
		NeoForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone evt) {
		ITechnology cap1 = evt.getOriginal().getData(TECH_CAP.get());
		ITechnology cap2 = evt.getEntity().getData(TECH_CAP.get());

		cap2.setResearched(cap1.getResearched());
		if (!cap1.isNew())
			cap2.setOld();
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
