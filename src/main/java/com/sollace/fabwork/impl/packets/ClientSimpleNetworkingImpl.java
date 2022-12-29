package com.sollace.fabwork.impl.packets;

import java.util.function.Function;

import com.sollace.fabwork.api.packets.Packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ClientSimpleNetworkingImpl {
    public static <T extends Packet<PlayerEntity>> void register(Identifier id, Function<PacketByteBuf, T> factory) {
        ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
            T packet = factory.apply(buffer);
            client.execute(() -> packet.handle(client.player));
        });
    }
}
