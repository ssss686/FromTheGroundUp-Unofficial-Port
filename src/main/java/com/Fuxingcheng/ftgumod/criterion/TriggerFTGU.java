package com.Fuxingcheng.ftgumod.criterion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.Fuxingcheng.ftgumod.util.ListenerTechnology;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public abstract class TriggerFTGU<T extends CriterionTriggerInstance> implements CriterionTrigger<T> {

	protected final Map<PlayerAdvancements, Set<Listener<T>>> listeners = new HashMap<>();
	protected final Map<PlayerAdvancements, Set<ListenerTech<T>>> techListeners = new HashMap<>();
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
