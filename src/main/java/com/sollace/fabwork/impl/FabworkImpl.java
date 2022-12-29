package com.sollace.fabwork.impl;

import java.util.Optional;
import java.util.stream.Stream;

import com.sollace.fabwork.api.*;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;

public class FabworkImpl implements Fabwork {
    public static final FabworkImpl INSTANCE = new FabworkImpl();
    private static final String CUSTOM_VALUES_KEY = "fabwork";
    private static final String REQUIREMENT_KEY = "requiredOn";

    @Override
    public RequirementType getRequirementForMod(String modId) {
        return FabricLoader.getInstance().getModContainer(modId).map(this::getRequirementFor).orElse(RequirementType.NONE);
    }

    public RequirementType getRequirementFor(ModContainer mod) {
        return Optional.ofNullable(mod.getMetadata().getCustomValue(CUSTOM_VALUES_KEY))
                .map(CustomValue::getAsObject)
                .map(v -> v.get(REQUIREMENT_KEY))
                .map(CustomValue::getAsString)
                .map(RequirementType::forKey)
                .orElse(RequirementType.NONE);
    }

    @Override
    public Stream<ModEntryImpl> getInstalledMods() {
        return FabricLoader.getInstance().getAllMods().stream().map(ModEntryImpl::new);
    }
}
