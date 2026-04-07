package com.sollace.fabwork.api.packets;

import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A client packet type. Sent by the server to a specific player.
 */
public record S2CPacketType<T> (
        CustomPacketPayload.Type<Payload<T>> id,
        StreamCodec<RegistryFriendlyByteBuf, Payload<T>> codec,
        Receiver<? extends Player, T> receiver
    ) {
    public void sendToPlayer(T packet, ServerPlayer recipient) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        ServerPlayNetworking.send(recipient, new Payload<>(packet, id));
    }

    public void sendToAllPlayers(T packet, Level world) {
        Objects.requireNonNull(world, "Server world cannot be null");
        var p = toPacket(packet);
        world.players().forEach(player -> {
            if (player instanceof ServerPlayer spe) {
                spe.connection.send(p);
            }
        });
    }

    public void sendToSurroundingPlayers(T packet, Entity entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        if (entity.level() instanceof ServerLevel sw) {
            sw.getChunkSource().sendToTrackingPlayersAndSelf(entity, toPacket(packet));
        }
    }

    public void sendToAllPlayers(T packet, MinecraftServer server) {
        Objects.requireNonNull(server, "Server cannot be null");
        var p = toPacket(packet);
        server.getPlayerList().getPlayers().forEach(recipient -> {
            recipient.connection.send(p);
        });
    }

    /**
     * Repackages a fabwork packet into a normal Minecraft protocol packet suitable for sending to a connected client.
     */
    public net.minecraft.network.protocol.Packet<ClientCommonPacketListener> toPacket(T packet) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        return ServerPlayNetworking.createClientboundPacket(new Payload<>(packet, id));
    }
}