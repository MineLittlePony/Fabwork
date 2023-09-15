package com.sollace.fabwork.impl.packets;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.NotImplementedException;
import com.sollace.fabwork.api.packets.*;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public final class Receivers {
    private Receivers() { throw new RuntimeException("new Receivers()"); }

    public static <Sender, P extends Packet> Receiver<Sender, P> empty(Identifier id) {
        return new Receiver<>() {
            @Override
            public void addPersistentListener(BiConsumer<Sender, P> callback) {
                throw new NotImplementedException("Receiving on the packet type '" + id + "' is not supported on side " + FabricLoader.getInstance().getEnvironmentType());
            }

            @Override
            public void addTemporaryListener(BiPredicate<Sender, P> callback) {
                throw new NotImplementedException("Receiving on the packet type '" + id + "' is not supported on side " + FabricLoader.getInstance().getEnvironmentType());
            }
        };
    }
}
