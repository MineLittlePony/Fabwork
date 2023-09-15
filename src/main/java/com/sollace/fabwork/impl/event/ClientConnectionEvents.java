package com.sollace.fabwork.impl.event;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * @Deprecated These events are superceded by the configuration stage.
 *             Rather use ClientConfigurationConnectionEvents#READY to respond when the player is joining
 */
@Deprecated(forRemoval = true)
public interface ClientConnectionEvents {
    /**
     * An event for notification when a player has successfully connected to the server.
     * <p>
     * This event is triggered after JOIN once initial game data has been sent to the client.
     */
    @Deprecated
    Event<Join> CONNECT = EventFactory.createArrayBacked(Join.class, callbacks -> (handler, sender, server) -> {
        for (Join callback : callbacks) {
            callback.onPlayReady(handler, sender, server);
        }
    });
}
