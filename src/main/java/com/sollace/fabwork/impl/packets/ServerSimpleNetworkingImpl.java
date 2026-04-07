package com.sollace.fabwork.impl.packets;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;
import com.sollace.fabwork.api.packets.*;
import com.sollace.fabwork.impl.ClientConnectionAccessor;
import com.sollace.fabwork.impl.PlayPingSynchroniser;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class ServerSimpleNetworkingImpl {
    private ServerSimpleNetworkingImpl() { throw new RuntimeException("new ServerSimpleNetworkingImpl()"); }

    public static <T> C2SPacketType<T> registerC2S(Identifier id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        var packetId = new CustomPacketPayload.Type<Payload<T>>(id);
        var type = new C2SPacketType<>(packetId, Payload.createCodec(packetId, codec), new ReceiverImpl<>(id));
        PayloadTypeRegistry.clientboundPlay().register(packetId, type.codec());
        ServerPlayNetworking.registerGlobalReceiver(packetId, (payload, context) -> {
            context.player().level().getServer().execute(() -> ((ReceiverImpl<ServerPlayer, T>)type.receiver()).onReceive(context.player(), payload.packet()));
        });
        return type;
    }

    public static <T> S2CPacketType<T> registerS2C(Identifier id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        var packetId = new CustomPacketPayload.Type<Payload<T>>(id);
        var type = new S2CPacketType<>(packetId, Payload.createCodec(packetId, codec), Receivers.empty(id));
        PayloadTypeRegistry.serverboundPlay().register(packetId, type.codec());
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T> Future<T> waitForReponse(C2SPacketType<T> packetType, Connection connection) {
        Objects.requireNonNull(connection, "Client Connection cannot be null");

        if (!connection.isConnected()) {
            return CompletableFuture.failedFuture(new IOException("Connection is closed"));
        }

        final Object[] receivedPacket = new Object[1];
        final CompletableFuture<T> future = new CompletableFuture<>();

        packetType.receiver().addTemporaryListener((sender, packet) -> {
            if (ClientConnectionAccessor.get(sender.connection) == connection) {
                receivedPacket[0] = packet;
                return true;
            }
            return !future.isDone();
        });

        PlayPingSynchroniser.waitForClientResponse(connection, responseType -> {
            if (receivedPacket[0] == null || responseType == PlayPingSynchroniser.ResponseType.ABORTED) {
                future.completeExceptionally(new TimeoutException());
            } else {
                future.complete((T)receivedPacket[0]);
            }
        });

        return future;
    }
}
