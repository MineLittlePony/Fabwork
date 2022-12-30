package com.sollace.fabwork.impl;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.NotImplementedException;

import com.sollace.fabwork.api.packets.Packet;
import com.sollace.fabwork.api.packets.Receiver;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ReceiverImpl<P extends PlayerEntity, T extends Packet<P>> implements Receiver<P, T> {
    private final ListenerList persistentListeners = new ListenerList();
    private final ListenerList listeners = new ListenerList();

    public static <P extends PlayerEntity, T extends Packet<P>> Receiver<P, T> empty(Identifier id) {
        return new Receiver<>() {
            @Override
            public void addPersistentListener(BiConsumer<P, T> callback) {
                throw new NotImplementedException("Receiving on the packet type '" + id + "' is not supported on side " + FabricLoader.getInstance().getEnvironmentType());
            }

            @Override
            public void addTemporaryListener(BiPredicate<P, T> callback) {
                throw new NotImplementedException("Receiving on the packet type '" + id + "' is not supported on side " + FabricLoader.getInstance().getEnvironmentType());
            }
        };
    }

    @Override
    public void addPersistentListener(BiConsumer<P, T> callback) {
        persistentListeners.enqueue((p, t) -> {
            try {
                callback.accept(p, t);
                return false;
            } catch (Exception e) {}
            return true;
        });
    }

    @Override
    public void addTemporaryListener(BiPredicate<P, T> callback) {
        listeners.enqueue((p, t) -> {
            try {
                return callback.test(p, t);
            } catch (Exception e) {}
            return true;
        });
    }

    public void onReceive(P sender, T packet) {
        persistentListeners.fire(sender, packet);
        listeners.fire(sender, packet);
        if (packet instanceof HandledPacket) {
            ((HandledPacket<P>)packet).handle(sender);
        }
    }

    final class ListenerList {
        private final Object lock = new Object();
        private final List<BiPredicate<P, T>> listeners = new ArrayList<>();

        void enqueue(BiPredicate<P, T> listener) {
            synchronized (lock) {
                listeners.add(listener);
            }
        }

        void fire(P sender, T packet) {
            final List<BiPredicate<P, T>> handlers;
            synchronized (lock) {
                handlers = new ArrayList<>(listeners);
                listeners.clear();
            }
            handlers.removeIf(listener -> listener.test(sender, packet));
            synchronized (lock) {
                listeners.addAll(handlers);
            }
        }
    }
}
