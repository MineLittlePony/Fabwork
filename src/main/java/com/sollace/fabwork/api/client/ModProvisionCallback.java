package com.sollace.fabwork.api.client;

import com.sollace.fabwork.api.ModEntry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Represents a callback invoked after joining a server with this mod installed.
 * <p>
 * This event serves as a notification that the server supports functionality of a certain mod.
 */
public interface ModProvisionCallback {
    Event<ModProvisionCallback> EVENT = EventFactory.createArrayBacked(ModProvisionCallback.class, callbacks -> (entry, available) -> {
        for (ModProvisionCallback callback : callbacks) {
            callback.onModProvisioned(entry, available);
        }
    });

    /**
     * Called when a mod is provisioned.
     *
     * @param entry     A mod entry representing the mod that was provisioned.
     * @param available Boolean indicating whether the mod is available on the server.
     */
    void onModProvisioned(ModEntry entry, boolean available);
}
