package com.sollace.fabwork.impl.packets;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sollace.fabwork.api.packets.*;

import net.minecraft.util.Identifier;

final class ReceiverImpl<Sender, P extends Packet> implements Receiver<Sender, P> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ListenerList persistentListeners = new ListenerList();
    private final ListenerList listeners = new ListenerList();

    private final Identifier id;

    ReceiverImpl(Identifier id) {
        this.id = id;
    }

    @Override
    public void addPersistentListener(BiConsumer<Sender, P> callback) {
        persistentListeners.enqueue((p, t) -> {
            callback.accept(p, t);
            return false;
        });
    }

    @Override
    public void addTemporaryListener(BiPredicate<Sender, P> callback) {
        listeners.enqueue(callback);
    }

    @SuppressWarnings("unchecked")
    void onReceive(Sender sender, P packet) {
        persistentListeners.fire(sender, packet);
        listeners.fire(sender, packet);
        if (packet instanceof HandledPacket) {
            try {
                ((HandledPacket<Sender>)packet).handle(sender);
            } catch (Exception e) {
                LOGGER.error("Exception whilst handling packet callback for handled packet " + id, e);
            }
        }
    }

    final class ListenerList {
        private final List<BiPredicate<Sender, P>> listeners = new ArrayList<>();

        void enqueue(BiPredicate<Sender, P> listener) {
            synchronized (this) {
                listeners.add(listener);
            }
        }

        void fire(Sender sender, P packet) {
            final List<BiPredicate<Sender, P>> handlers;
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
