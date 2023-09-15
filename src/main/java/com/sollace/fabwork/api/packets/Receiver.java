package com.sollace.fabwork.api.packets;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * A receiver for subscribing to packet responses beyond just the initial global method.
 * <p>
 * Using this mod that register a packet type may then either use it's receiver as
 * an event bus to subscribe to responses, or expose it for use by others without
 * them having to register any additional receivers.
 *
 * @param <Sender> The sending player
 * @param <P> The packet type received.
 */
public interface Receiver<Sender, P extends Packet> {
    /**
     * Adds a persistent listener.
     * Once registered, these listeners will remain registered for the lifetime of the client or until they throw an exception.
     * Callers typically should not use this if they intend on removing their listener at a later point. For those, rather
     * use {@link addTemporaryListener} and return {@code true} to indicate when their listener can be disposed.
     *
     * <p>
     * Calling this is the equivalent of calling:
     * {@code addTemporaryListener((p, t) -> {
     *    callback.accept(p, t);
     *    return false;
     * });}
     *
     * @param callback The callback to execute upon receipt of a packet from this receiver.
     */
    void addPersistentListener(BiConsumer<Sender, P> callback);

    /**
     * Adds a temporary listener.
     * Once registered, the listener will continue to function until ti returns {@code true} at which point the listener may then be disposed.
     *
     * @param callback The callback to execute upon receipt of a packet from this receiver.
     */
    void addTemporaryListener(BiPredicate<Sender, P> callback);
}
