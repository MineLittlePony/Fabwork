package com.sollace.fabwork.api.packets;

import java.util.Objects;
import java.util.function.Function;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * A client packet type. Sent by the server to a specific player.
 */
public record S2CPacketType<T extends Packet<? extends PlayerEntity>> (
        Identifier id,
        Function<PacketByteBuf, T> constructor,
        Receiver<? extends PlayerEntity, T> receiver
    ) {
    public void sendToPlayer(T packet, ServerPlayerEntity recipient) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        ServerPlayNetworking.send(recipient, id(), packet.toBuffer());
    }

    public void sendToAllPlayers(T packet, World world) {
        Objects.requireNonNull(world, "Server world cannot be null");
        var p = toPacket(packet);
        world.getPlayers().forEach(player -> {
            if (player instanceof ServerPlayerEntity spe) {
                spe.networkHandler.sendPacket(p);
            }
        });
    }

    public void sendToSurroundingPlayers(T packet, Entity entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");
        if (entity.getWorld() instanceof ServerWorld sw) {
            sw.getChunkManager().sendToNearbyPlayers(entity, toPacket(packet));
        }
    }


    /**
     * Repackages a fabwork packet into a normal Minecraft protocol packet suitable for sending to a connected client.
     */
    public net.minecraft.network.Packet<ClientPlayPacketListener> toPacket(T packet) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        return ServerPlayNetworking.createS2CPacket(id(), packet.toBuffer());
    }
}