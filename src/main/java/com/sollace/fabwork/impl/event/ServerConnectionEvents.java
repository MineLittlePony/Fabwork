package com.sollace.fabwork.impl.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;

/**
 * @Deprecated These events are superceded by the configuration stage.
 *             Rather use ServerConfigurationConnectionEvents#CONFIGURE to respond to a joining player
 */
@Deprecated(forRemoval = true)
public interface ServerConnectionEvents {
    /**
     * An event for notification when a player has successfully connected to the server.
     * <p>
     * This event is triggered after JOIN but before the corresponding event packet is dispatched to the client.
     */
    Event<Join> CONNECT = EventFactory.createArrayBacked(Join.class, callbacks -> (handler, sender, server) -> {
        for (Join callback : callbacks) {
            callback.onPlayReady(handler, sender, server);
        }
    });
}
