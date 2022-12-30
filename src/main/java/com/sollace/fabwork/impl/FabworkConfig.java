package com.sollace.fabwork.impl;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.gson.*;
import com.sollace.fabwork.api.RequirementType;

class FabworkConfig {
    private static final Gson GSON = new GsonBuilder().create();

    public static FabworkConfig load(Path path) {
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                return GSON.fromJson(reader, FabworkConfig.class);
            } catch (IOException | JsonParseException e) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e1) {}
            }
        }

        FabworkConfig config = new FabworkConfig();
        try {

            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {}
        return config;
    }

    @Nullable
    public List<String> requiredModIds;

    public Stream<ModEntryImpl> getCustomRequiredMods() {
        if (requiredModIds == null || requiredModIds.isEmpty()) {
            return Stream.empty();
        }

        return requiredModIds.stream().map(id -> new ModEntryImpl(id, RequirementType.CLIENT));
    }
}
