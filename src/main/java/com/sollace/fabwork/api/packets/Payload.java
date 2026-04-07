package com.sollace.fabwork.api.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record Payload<P>(P packet, Type<Payload<P>> type) implements CustomPacketPayload {
    @SuppressWarnings("unchecked")
    public static <P> StreamCodec<RegistryFriendlyByteBuf, Payload<P>> createCodec(Type<Payload<P>> type, StreamCodec<? super RegistryFriendlyByteBuf, P> packetCodec) {
        return ((StreamCodec<RegistryFriendlyByteBuf, P>)packetCodec).map(
                p -> new Payload<P>(p, type),
                Payload::packet
        );
    }
}
