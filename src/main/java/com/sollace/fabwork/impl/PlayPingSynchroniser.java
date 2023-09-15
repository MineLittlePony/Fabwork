package com.sollace.fabwork.impl;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;

public class PlayPingSynchroniser {
    private static final Long2ObjectMap<Consumer<ResponseType>> PENDING_CALLBACKS = new Long2ObjectLinkedOpenHashMap<>();

    @Nullable
    private static Consumer<ResponseType> dequeueResponseCallback(long id) {
        synchronized (PENDING_CALLBACKS) {
            return PENDING_CALLBACKS.remove(id);
        }
    }

    private static int enqueueReponseCallback(Consumer<ResponseType> callback) {
        synchronized (PENDING_CALLBACKS) {
            int id = (int)System.currentTimeMillis();
            PENDING_CALLBACKS.put(id, callback);
            return id;
        }
    }

    public static void waitForClientResponse(ClientConnection connection, Consumer<ResponseType> callback) {
        if (connection.isOpen()) {
            connection.send(new CommonPingS2CPacket(enqueueReponseCallback(callback)));
        } else {
            callback.accept(ResponseType.ABORTED);
        }
    }

    public static void onClientResponse(CommonPongC2SPacket packet, Executor executor) {
        Consumer<ResponseType> callback = dequeueResponseCallback(packet.getParameter());
        if (callback != null) {
            executor.execute(() -> callback.accept(ResponseType.COMPLETED));
        }
    }

    public enum ResponseType {
        COMPLETED,
        ABORTED
    }
}
