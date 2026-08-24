package com.fuxingcheng.fromthegroundup.event;

import com.fuxingcheng.fromthegroundup.technology.Technology;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import net.minecraft.world.entity.player.Player;

public abstract class TechnologyEvent {

	private final Player player;
	private final Technology tech;

	public TechnologyEvent(Player player, Technology tech) {
		this.player = player;
		this.tech = tech;
	}

	public Player getPlayer() {
		return player;
	}

	public Technology getTechnology() {
		return tech;
	}

	/**
	 * Fires after a Technology is researched, using commands or with the research table
	 */
	public static class Research extends TechnologyEvent {

		public static final Event<Consumer> EVENT = EventFactory.createArrayBacked(Consumer.class,
				listeners -> event -> {
					for (Consumer listener : listeners) {
						listener.accept(event);
					}
				});

		public Research(Player player, Technology tech) {
			super(player, tech);
		}

		public interface Consumer {
			void accept(Research event);
		}
	}

	/**
	 * Fires after a Technology is unlocked
	 */
	public static class Unlock extends TechnologyEvent {

		public static final Event<Consumer> EVENT = EventFactory.createArrayBacked(Consumer.class,
				listeners -> event -> {
					for (Consumer listener : listeners) {
						listener.accept(event);
					}
				});

		public Unlock(Player player, Technology tech) {
			super(player, tech);
		}

		public interface Consumer {
			void accept(Unlock event);
		}
	}

	/**
	 * Fires after a Technology (or unlock) has been revoked
	 */
	public static class Revoke extends TechnologyEvent {

		public static final Event<Consumer> EVENT = EventFactory.createArrayBacked(Consumer.class,
				listeners -> event -> {
					for (Consumer listener : listeners) {
						listener.accept(event);
					}
				});

		public Revoke(Player player, Technology tech) {
			super(player, tech);
		}

		public interface Consumer {
			void accept(Revoke event);
		}
	}

}
