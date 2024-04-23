package com.sollace.fabwork.api.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record Payload<P extends Packet>(P packet, CustomPayload.Id<Payload<P>> id) implements CustomPayload {
    public static <P extends Packet> PacketCodec<PacketByteBuf, Payload<P>> createCodec(Id<Payload<P>> id, PacketCodec<PacketByteBuf, P> packetCodec) {
        return packetCodec.<Payload<P>>xmap(
                p -> new Payload<P>(p, id),
                p -> p.packet()
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return id;
    }
}
