package com.fuxingcheng.fromthegroundup.criterion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fuxingcheng.fromthegroundup.util.ListenerTechnology;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public abstract class TriggerFTGU<T extends CriterionTriggerInstance> implements CriterionTrigger<T> {

	protected final Map<PlayerAdvancements, Set<Listener<T>>> listeners = new HashMap<>();
	protected final Map<PlayerAdvancements, Set<TriggerFTGU.ListenerTech<T>>> techListeners = new HashMap<>();
	private final ResourceLocation id;

	public TriggerFTGU(ResourceLocation id) {
		this.id = id;
	}

	public ResourceLocation getId() {
		return id;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, Listener<T> listener) {
		listeners.computeIfAbsent(playerAdvancements, p -> new HashSet<>()).add(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, Listener<T> listener) {
		Set<Listener<T>> set = listeners.get(playerAdvancements);
		if (set != null) {
			set.remove(listener);
			if (set.isEmpty())
				listeners.remove(playerAdvancements);
		}
	}

	@Override
	public void removePlayerListeners(PlayerAdvancements playerAdvancements) {
		listeners.remove(playerAdvancements);
		techListeners.remove(playerAdvancements);
	}

	/**
	 * 遍历用的快照。原版在 grant 之后会立刻 unregisterListener，直接遍历原集合会抛
	 * ConcurrentModificationException（和原版 SimpleCriterionTrigger 先 toArray 是一个道理）。
	 */
	protected Set<Listener<T>> snapshotListeners(PlayerAdvancements advancements) {
		Set<Listener<T>> set = listeners.get(advancements);
		return set == null ? Set.of() : new HashSet<>(set);
	}

	protected Set<ListenerTech<T>> snapshotTechListeners(PlayerAdvancements advancements) {
		Set<ListenerTech<T>> set = techListeners.get(advancements);
		return set == null ? Set.of() : new HashSet<>(set);
	}

	public void addTechListener(PlayerAdvancements pa, T instance, ListenerTechnology listenerTech) {
		techListeners.computeIfAbsent(pa, p -> new HashSet<>())
				.add(new ListenerTech<>(instance, listenerTech));
	}

	public void removeTechListener(PlayerAdvancements pa, T instance, ListenerTechnology listenerTech) {
		Set<ListenerTech<T>> set = techListeners.get(pa);
		if (set != null) {
			set.remove(new ListenerTech<>(instance, listenerTech));
			if (set.isEmpty())
				techListeners.remove(pa);
		}
	}

	public record ListenerTech<T extends CriterionTriggerInstance>(T triggerInstance, ListenerTechnology listenerTechnology) {
	}

}
