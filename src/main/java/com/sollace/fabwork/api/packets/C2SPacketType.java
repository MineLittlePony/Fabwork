package com.sollace.fabwork.api.packets;

import com.google.common.base.Preconditions;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A server packet type. Sent by the client to the server.
 */
public record C2SPacketType<T extends Packet<ServerPlayerEntity>> (Identifier id) {
    public void sendToServer(T packet) {
        Preconditions.checkState(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT, "Client packet send called by the server");
        ClientPlayNetworking.send(id(), packet.toBuffer());
    }
}