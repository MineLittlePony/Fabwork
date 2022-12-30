package com.sollace.fabwork.impl;

import java.util.Set;
import java.util.function.Consumer;
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
                try {
                    invokation.accept(initializer.getEntrypoint());
                } catch (Throwable t) {
                    LOGGER.fatal("Exception occured whilst invoking initializer for {} provided by {}", key, initializer.getProvider().getMetadata().getId());
                }
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
}
