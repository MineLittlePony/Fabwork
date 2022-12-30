package com.sollace.fabwork.api.packets;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.sollace.fabwork.impl.packets.ClientSimpleNetworkingImpl;
import com.sollace.fabwork.impl.packets.ServerSimpleNetworkingImpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A server-beound packet type. Sent by the client to the server.
 * <p>
 * The handler for packets registered with this type are executed on the server's main thread
 * and receive the originating ServerPlayerEntity.
 * <p>
 * Responses can be sent back to the sending player by calling the appropriate send method on a S2CPacketType.
 */
public record C2SPacketType<T extends Packet<ServerPlayerEntity>> (
        Identifier id,
        Function<PacketByteBuf, T> constructor,
        Receiver<ServerPlayerEntity, T> receiver
    ) {
    /**
     * Sends a packet to be handled by the server.
     *
     * @sideOnly Client
     */
    public void sendToServer(T packet) {
        Preconditions.checkState(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT, "Client packet send called by the server");
        ClientSimpleNetworkingImpl.send(id(), packet.toBuffer());
    }

    /**
     * Creates a single-use callback to invoke when receiving a packet from a particular client.
     * The returned future will either complete successfully with the incoming packet,
     * or fail upon a timeout or closed connection.
     * <p>
     * Callback completion is performed on the main thread.
     *
     * @param client The client entity to expect a response from.
     * @return A future representing the pending incoming request.
     */
    public Future<T> awaitResponseFrom(ServerPlayerEntity client) {
        Objects.requireNonNull(client, "Client player cannot be null");
        return ServerSimpleNetworkingImpl.waitForReponse(this, client.networkHandler.getConnection());
    }

    /**
     * Repackages a Fabwork packet into a normal Minecraft protocol packet suitable for sending to the connected server.
     */
    public net.minecraft.network.Packet<ServerPlayPacketListener> toPacket(T packet) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        return ClientSimpleNetworkingImpl.createC2SPacket(id(), packet.toBuffer());
    }
}