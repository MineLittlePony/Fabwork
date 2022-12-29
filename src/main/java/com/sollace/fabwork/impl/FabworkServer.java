package com.sollace.fabwork.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Streams;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class FabworkServer implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Fabwork");
    public static final Identifier CONSENT_ID = id("synchronize");
    public static final int PROTOCOL_VERSION = 1;

    @Override
    public void onInitialize() {
        final FabworkConfig config = FabworkConfig.load(FabricLoader.getInstance().getConfigDir().resolve("fabwork.json"));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            LOGGER.info("Sending synchronize packet to " + handler.getPlayer().getName().getString());
            sender.sendPacket(CONSENT_ID, ModEntryImpl.write(
                    makeDistinct(Streams.concat(FabworkImpl.INSTANCE.getInstalledMods().filter(ModEntryImpl::requiredOnEither), config.getCustomRequiredMods())),
                    PacketByteBufs.create())
            );
        });
    }

    private static Stream<ModEntryImpl> makeDistinct(Stream<ModEntryImpl> entries) {
        Map<String, ModEntryImpl> map = new HashMap<>();
        entries.forEach(entry -> {
            map.compute(entry.modId(), (id, value) -> value == null || entry.requirement().supercedes(value.requirement()) ? entry : value);
        });
        return map.values().stream();
    }

    public static Identifier id(String name) {
        return new Identifier("fabwork", name);
    }
}
