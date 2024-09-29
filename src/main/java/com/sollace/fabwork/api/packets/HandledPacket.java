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
 * Recommended approach is to use {@link HandledPacket} and override handle(sender) for server-bound
 * packets and use {@link Packet} together with the receiver API
 * to handle client-bound packets as this allows for better separation of client-specific code.
 *
 * @author Sollace
 * @deprecated The packet interface will be retired in 1.22. Use Handled<Sender> instead.
 */
@Deprecated
public interface HandledPacket<Sender> extends Packet, Handled<Sender> {
}