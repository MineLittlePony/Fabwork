package com.sollace.fabwork.impl;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sollace.fabwork.api.packets.*;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ReceiverImpl<P extends PlayerEntity, T extends Packet<P>> implements Receiver<P, T> {
    private static final Logger LOGGER = LogManager.getLogger();

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

    private final ListenerList persistentListeners = new ListenerList();
    private final ListenerList listeners = new ListenerList();

    private final Identifier id;

    public ReceiverImpl(Identifier id) {
        this.id = id;
    }

    @Override
    public void addPersistentListener(BiConsumer<P, T> callback) {
        persistentListeners.enqueue((p, t) -> {
            try {
                callback.accept(p, t);
                return false;
            } catch (Exception e) {
                LOGGER.error("Exception whilst handling packet callback for packet " + id, e);
            }
            return true;
        });
    }

    @Override
    public void addTemporaryListener(BiPredicate<P, T> callback) {
        listeners.enqueue((p, t) -> {
            try {
                return callback.test(p, t);
            } catch (Exception e) {
                LOGGER.error("Exception whilst handling packet callback for packet " + id, e);
            }
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    public void onReceive(P sender, T packet) {
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
