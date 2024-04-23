package com.sollace.fabwork.impl.packets;

import java.util.function.Function;

import com.sollace.fabwork.api.packets.Packet;
import com.sollace.fabwork.api.packets.Payload;
import com.sollace.fabwork.api.packets.S2CPacketType;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class ClientSimpleNetworkingImpl {
    private ClientSimpleNetworkingImpl() { throw new RuntimeException("new ClientSimpleNetworkingImpl()"); }

    @SuppressWarnings("unchecked")
    public static <T extends Packet> S2CPacketType<T> register(Identifier id, Function<PacketByteBuf, T> factory) {
        var packetId = new CustomPayload.Id<Payload<T>>(id);
        var type = new S2CPacketType<>(packetId, Payload.createCodec(packetId, PacketCodec.of(Packet::toBuffer, factory::apply)), new ReceiverImpl<>(id));
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
