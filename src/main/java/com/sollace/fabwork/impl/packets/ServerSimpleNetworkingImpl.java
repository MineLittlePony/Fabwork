package com.sollace.fabwork.impl.packets;

import java.util.function.Function;

import com.sollace.fabwork.api.packets.Packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ServerSimpleNetworkingImpl {
    public static <T extends Packet<ServerPlayerEntity>> void register(Identifier id, Function<PacketByteBuf, T> factory) {
        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, responder) -> {
            T packet = factory.apply(buffer);
            server.execute(() -> packet.handle(player));
        });
    }
}
