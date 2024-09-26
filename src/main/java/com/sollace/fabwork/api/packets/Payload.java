package com.sollace.fabwork.api.packets;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record Payload<P>(P packet, CustomPayload.Id<Payload<P>> id) implements CustomPayload {
    @SuppressWarnings("unchecked")
    public static <P> PacketCodec<RegistryByteBuf, Payload<P>> createCodec(Id<Payload<P>> id, PacketCodec<? super RegistryByteBuf, P> packetCodec) {
        return ((PacketCodec<RegistryByteBuf, P>)packetCodec).<Payload<P>>xmap(
                p -> new Payload<P>(p, id),
                p -> p.packet()
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return id;
    }
}
