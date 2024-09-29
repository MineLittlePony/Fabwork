package com.sollace.fabwork.api.packets;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

/**
 * Represents a message that can be either sent from the client to the server or back.
 * <p>
 * A recommended approach is to use {@link HandledPacket} and override handle(sender) for server-bound
 * packets and use {@link Packet} together with the receiver API
 * to handle client-bound packets as this allows for better separation of client-specific code.
 *
 * @author Sollace
 * @deprecated The packet interface will be retired in 1.22. Use Handled<Sender> instead.
 */
@Deprecated
public interface Packet {
    /**
     * Writes this packet to the supplied buffer prior to transmission.
     *
     * @param buffer The buffer to write to.
     */
    void toBuffer(PacketByteBuf buffer);

    /**
     * Writes this packet to a new buffer.
     *
     * @return The resulting buffer for transmission
     */
    default PacketByteBuf toBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        toBuffer(buf);
        return buf;
    }
}