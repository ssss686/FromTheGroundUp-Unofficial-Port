package com.fuxingcheng.fromthegroundup.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class FTGUClientSyncEvent {

	/**
	 * This event is fired on the client after FTGU syncs a player's technology
	 * update.
	 */
	public static final Event<Runnable> POST = EventFactory.createArrayBacked(Runnable.class,
			listeners -> () -> {
				for (Runnable listener : listeners) {
					listener.run();
				}
			});

	private FTGUClientSyncEvent() {
		//
	}
}
