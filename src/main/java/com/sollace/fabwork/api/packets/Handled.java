package com.sollace.fabwork.api.packets;

/**
 * A special sub-class of a packet that provides its own handler.
 * <p>
 * This is provided as an alternative to using the receiver() approach to
 * registering callbacks.
 * <p>
 * Using HandledPacket<P> may be preferred for modders that would like to
 * more-closely mirror what Mojang has done in their networking code,
 * or if the packet class they're creating extends a pre-established
 * packet from the base game.
 * <p>
 * Recommended approach is to use {@link Handled} and override handle(sender) for server-bound
 * packets and use the receiver API to handle client-bound packets as this allows for better separation of client-specific code.
 *
 * @author Sollace
 */
public interface Handled<Sender> {
    /**
     * Called to handle this packet on the receiving end.
     * <p>
     * Implementors may optionally override this method,
     * or register handlers using the receiver().
     * <p>
     *
     * @param sender The player who initially sent this packet.
     */
    void handle(Sender sender);
}
