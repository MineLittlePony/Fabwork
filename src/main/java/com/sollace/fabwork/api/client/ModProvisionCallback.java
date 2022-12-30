package com.sollace.fabwork.api.client;

import com.sollace.fabwork.api.ModEntry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Represents a callback invoked after joining a server supporting fabwork's protocol.
 * <p>
 * This event serves as a notification of which mods the joined server supports.
 *
 * @author Sollace
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
