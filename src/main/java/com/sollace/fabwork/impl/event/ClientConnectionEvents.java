package com.sollace.fabwork.impl.event;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ClientConnectionEvents {
    /**
     * An event for notification when a player has successfully connected to the server.
     * <p>
     * This event is triggered after JOIN once initial game data has been sent to the client.
     */
    Event<Join> CONNECT = EventFactory.createArrayBacked(Join.class, callbacks -> (handler, sender, server) -> {
        for (Join callback : callbacks) {
            callback.onPlayReady(handler, sender, server);
        }
    });
}
