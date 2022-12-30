package com.sollace.fabwork.impl.packets;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sollace.fabwork.api.packets.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

final class ReceiverImpl<P extends PlayerEntity, T extends Packet<P>> implements Receiver<P, T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ListenerList persistentListeners = new ListenerList();
    private final ListenerList listeners = new ListenerList();

    private final Identifier id;

    ReceiverImpl(Identifier id) {
        this.id = id;
    }

    @Override
    public void addPersistentListener(BiConsumer<P, T> callback) {
        persistentListeners.enqueue((p, t) -> {
            callback.accept(p, t);
            return false;
        });
    }

    @Override
    public void addTemporaryListener(BiPredicate<P, T> callback) {
        listeners.enqueue(callback);
    }

    @SuppressWarnings("unchecked")
    void onReceive(P sender, T packet) {
        persistentListeners.fire(sender, packet);
        listeners.fire(sender, packet);
        if (packet instanceof HandledPacket) {
            try {
                ((HandledPacket<P>)packet).handle(sender);
            } catch (Exception e) {
                LOGGER.error("Exception whilst handling packet callback for handled packet " + id, e);
            }
        }
    }

    final class ListenerList {
        private final List<BiPredicate<P, T>> listeners = new ArrayList<>();

        void enqueue(BiPredicate<P, T> listener) {
            synchronized (this) {
                listeners.add(listener);
            }
        }

        void fire(P sender, T packet) {
            final List<BiPredicate<P, T>> handlers;
            synchronized (this) {
                handlers = new ArrayList<>(listeners);
                listeners.clear();
            }
            handlers.removeIf(listener -> {
                try {
                    return listener.test(sender, packet);
                } catch (Exception e) {
                    LOGGER.error("Exception whilst handling packet callback for packet " + id, e);
                    return true;
                }
            });
            synchronized (this) {
                listeners.addAll(handlers);
            }
        }
    }
}
