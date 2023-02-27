package com.sollace.fabwork.impl;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.google.gson.*;
import com.sollace.fabwork.api.RequirementType;

import net.fabricmc.loader.api.FabricLoader;

class FabworkConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Supplier<FabworkConfig> INSTANCE = Suppliers.memoize(() -> {
        return load(FabricLoader.getInstance().getConfigDir().resolve("fabwork.json"));
    });

    public static FabworkConfig load(Path path) {
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                return save(GSON.fromJson(reader, FabworkConfig.class), path);
            } catch (IOException | JsonParseException e) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e1) {}
            }
        }

        return save(new FabworkConfig(), path);
    }

    @Nullable
    private static FabworkConfig save(@Nullable FabworkConfig config, Path path) {
        if (config == null) {
            return config;
        }

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {}

        return config;
    }

    @Nullable
    public List<String> requiredModIds;

    public boolean enableJoinChecks;

    public Stream<ModEntryImpl> getCustomRequiredMods() {
        if (requiredModIds == null || requiredModIds.isEmpty()) {
            return Stream.empty();
        }

        return requiredModIds.stream().map(id -> new ModEntryImpl(id, RequirementType.CLIENT));
    }
}
