package com.sollace.fabwork.impl;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

class LoaderUtil {
    private static final Logger LOGGER = LogManager.getLogger("Fabwork::LOADER");

    static <EntryPoint> void invokeEntryPoints(String key, Class<EntryPoint> clazz, Consumer<EntryPoint> invokation) {
        try {
            FabricLoader.getInstance().getEntrypointContainers(key, clazz).forEach(initializer -> {
                invokeUntrusted(() -> {
                    invokation.accept(initializer.getEntrypoint());
                }, () -> "Exception occured whilst invoking initializer for " + key + " provided by " + initializer.getProvider().getMetadata().getId() + " {}");
            });
        } catch (Throwable t) {
            LOGGER.fatal("Could not get entrypoints for {}.", key);
        }
    }

    static <EntryPoint> void invokeDynamicEntryPoints(String baseKey, Class<EntryPoint> clazz, Consumer<EntryPoint> invokation) {
        final Set<String> modIds = FabricLoader.getInstance().getAllMods().stream()
                .map(container -> container.getMetadata().getId())
                .filter(id -> !"fabwork".contentEquals(id))
                .distinct()
                .collect(Collectors.toSet());
        invokeEntryPoints(baseKey, ClientModInitializer.class, ClientModInitializer::onInitializeClient);
        modIds.forEach(id -> invokeEntryPoints(baseKey + ":" + id, clazz, invokation));
    }

    static void invokeUntrusted(Runnable task, Supplier<String> untrustedMessage) {
        try {

        } catch (Throwable t) {
            String message = "Exception caught in unstrusted area {}";
            try {
                message = untrustedMessage.get();
            } catch (Throwable t2) {
                t.addSuppressed(t);
            }
            LOGGER.fatal(message, t);
        }
    }

    static void invokeUntrusted(Runnable task, String message) {
        invokeUntrusted(task, () -> message + " {}");
    }
}
