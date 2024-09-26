package com.sollace.fabwork.impl.packets;

import com.sollace.fabwork.api.packets.Payload;
import com.sollace.fabwork.api.packets.S2CPacketType;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class ClientSimpleNetworkingImpl {
    private ClientSimpleNetworkingImpl() { throw new RuntimeException("new ClientSimpleNetworkingImpl()"); }

    @SuppressWarnings("unchecked")
    public static <T> S2CPacketType<T> register(Identifier id, PacketCodec<? super RegistryByteBuf, T> codec) {
        var packetId = new CustomPayload.Id<Payload<T>>(id);
        var type = new S2CPacketType<>(packetId, Payload.createCodec(packetId, codec), new ReceiverImpl<>(id));
        PayloadTypeRegistry.playS2C().register(type.id(), type.codec());
        ClientPlayNetworking.registerGlobalReceiver(type.id(), (payload, context) -> {
            context.client().execute(() -> ((ReceiverImpl<PlayerEntity, T>)type.receiver()).onReceive(context.player(), payload.packet()));
        });
        return type;
    }

    public static void send(CustomPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static net.minecraft.network.packet.Packet<ServerCommonPacketListener> createC2SPacket(CustomPayload payload) {
        return ClientPlayNetworking.createC2SPacket(payload);
    }
}
