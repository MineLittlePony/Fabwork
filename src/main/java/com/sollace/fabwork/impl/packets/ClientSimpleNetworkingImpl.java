package com.sollace.fabwork.impl.packets;

import java.util.function.Function;

import com.sollace.fabwork.api.packets.Packet;
import com.sollace.fabwork.api.packets.S2CPacketType;
import com.sollace.fabwork.impl.ReceiverImpl;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.Identifier;

public class ClientSimpleNetworkingImpl {
    public static <T extends Packet<PlayerEntity>> S2CPacketType<T> register(Identifier id, Function<PacketByteBuf, T> factory) {
        ReceiverImpl<PlayerEntity, T> receiver = new ReceiverImpl<>(id);
        S2CPacketType<T> type = new S2CPacketType<>(id, factory, receiver);
        ClientPlayNetworking.registerGlobalReceiver(type.id(), (client, handler, buffer, responder) -> {
            T packet = type.constructor().apply(buffer);
            client.execute(() -> receiver.onReceive(client.player, packet));
        });
        return type;
    }

    public static void send(Identifier id, PacketByteBuf buffer) {
        ClientPlayNetworking.send(id, buffer);
    }

    public static net.minecraft.network.Packet<ServerPlayPacketListener> createC2SPacket(Identifier id, PacketByteBuf buffer) {
        return ClientPlayNetworking.createC2SPacket(id, buffer);
    }
}
