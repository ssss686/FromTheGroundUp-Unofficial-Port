package ftgumod.event;

import ftgumod.technology.Technology;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public abstract class TechnologyEvent extends PlayerEvent {

	private final Technology tech;

	public TechnologyEvent(Player player, Technology tech) {
		super(player);
		this.tech = tech;
	}

	public Technology getTechnology() {
		return tech;
	}

	/**
	 * Fires after a Technology is researched, using commands or with the research table
	 */
	public static class Research extends TechnologyEvent {

		public Research(Player player, Technology tech) {
			super(player, tech);
		}

	}

	/**
	 * Fires after a Technology is unlocked
	 */
	public static class Unlock extends TechnologyEvent {

		public Unlock(Player player, Technology tech) {
			super(player, tech);
		}

	}

	/**
	 * Fires after a Technology (or unlock) has been revoked
	 */
	public static class Revoke extends TechnologyEvent {

		public Revoke(Player player, Technology tech) {
			super(player, tech);
		}

	}

}
