package com.sollace.fabwork.impl.packets;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

import com.sollace.fabwork.api.packets.*;
import com.sollace.fabwork.impl.ClientConnectionAccessor;
import com.sollace.fabwork.impl.PlayPingSynchroniser;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ServerSimpleNetworkingImpl {
    private ServerSimpleNetworkingImpl() { throw new RuntimeException("new ServerSimpleNetworkingImpl()"); }

    public static <T extends Packet> C2SPacketType<T> register(Identifier id, Function<PacketByteBuf, T> factory) {
        ReceiverImpl<ServerPlayerEntity, T> receiver = new ReceiverImpl<>(id);
        C2SPacketType<T> type = new C2SPacketType<>(id, factory, receiver);
        ServerPlayNetworking.registerGlobalReceiver(type.id(), (server, player, handler, buffer, responder) -> {
            T packet = type.constructor().apply(buffer);
            server.execute(() -> receiver.onReceive(player, packet));
        });
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Packet> Future<T> waitForReponse(C2SPacketType<T> packetType, ClientConnection connection) {
        Objects.requireNonNull(connection, "Client Connection cannot be null");

        if (!connection.isOpen()) {
            return CompletableFuture.failedFuture(new IOException("Connection is closed"));
        }

        final Object[] receivedPacket = new Object[1];
        final CompletableFuture<T> future = new CompletableFuture<>();

        packetType.receiver().addTemporaryListener((sender, packet) -> {
            if (ClientConnectionAccessor.get(sender.networkHandler) == connection) {
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
