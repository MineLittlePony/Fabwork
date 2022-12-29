package com.sollace.fabwork.api;

import java.util.stream.Stream;

import com.sollace.fabwork.impl.FabworkImpl;

public interface Fabwork {
    Fabwork INSTANCE = FabworkImpl.INSTANCE;

    RequirementType getRequirementForMod(String modId);

    Stream<? extends ModEntry> getInstalledMods();
}
