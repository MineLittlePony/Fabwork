package com.sollace.fabwork.impl.packets;

import com.sollace.fabwork.api.packets.Payload;
import com.sollace.fabwork.api.packets.S2CPacketType;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class ClientSimpleNetworkingImpl {
    private ClientSimpleNetworkingImpl() { throw new RuntimeException("new ClientSimpleNetworkingImpl()"); }

    @SuppressWarnings("unchecked")
    public static <T> S2CPacketType<T> register(Identifier id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        var packetId = new CustomPacketPayload.Type<Payload<T>>(id);
        var type = new S2CPacketType<>(packetId, Payload.createCodec(packetId, codec), new ReceiverImpl<>(id));
        PayloadTypeRegistry.clientboundPlay().register(type.id(), type.codec());
        ClientPlayNetworking.registerGlobalReceiver(type.id(), (payload, context) -> {
            context.client().execute(() -> ((ReceiverImpl<Player, T>)type.receiver()).onReceive(context.player(), payload.packet()));
        });
        return type;
    }

    public static void send(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static net.minecraft.network.protocol.Packet<ServerCommonPacketListener> createC2SPacket(CustomPacketPayload payload) {
        return ClientPlayNetworking.createServerboundPacket(payload);
    }
}
