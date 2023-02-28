package com.sollace.fabwork.impl;

import java.util.Optional;
import java.util.stream.Stream;

import com.sollace.fabwork.api.*;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;

class FabworkImpl implements Fabwork {
    private static final String CUSTOM_VALUES_KEY = "fabwork";
    private static final String REQUIREMENT_KEY = "requiredOn";

    static final FabworkImpl INSTANCE = new FabworkImpl();

    @Override
    public RequirementType getRequirementForMod(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .filter(FabworkImpl::isValid)
                .map(FabworkImpl::getRequirementFor)
                .orElse(RequirementType.NONE);
    }

    static RequirementType getRequirementFor(ModContainer mod) {
        return Optional.ofNullable(mod.getMetadata().getCustomValue(CUSTOM_VALUES_KEY))
                .map(CustomValue::getAsObject)
                .map(v -> v.get(REQUIREMENT_KEY))
                .map(CustomValue::getAsString)
                .map(RequirementType::forKey)
                .orElse(RequirementType.NONE);
    }

    static boolean isValid(ModContainer mod) {
        return mod != null && mod.getMetadata() != null && mod.getMetadata().getId() != null;
    }

    @Override
    public Stream<ModEntryImpl> getInstalledMods() {
        return FabricLoader.getInstance().getAllMods().stream().filter(FabworkImpl::isValid).map(ModEntryImpl::new);
    }
}
