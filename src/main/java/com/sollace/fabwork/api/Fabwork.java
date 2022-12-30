package com.sollace.fabwork.api;

import java.util.stream.Stream;

import com.sollace.fabwork.impl.FabworkImpl;

/**
 *
 * For Client-Specific methods see {@link FabworkClient}
 *
 * @author Sollace
 *
 * @see com.sollace.fabwork.api.client.FabworkClient
 */
public interface Fabwork {
    Fabwork INSTANCE = FabworkImpl.INSTANCE;

    /**
     * Gets the requirement set by a mod in their custom metadata block.
     *
     *
     * @param modId The mod id of the mod to query.
     */
    RequirementType getRequirementForMod(String modId);

    /**
     * Gets a list of all installed mods and their requirements.
     * This is typically backed with the data provided by FabricLoader.getAllMods()
     *
     * @return A stream of ModEntry.
     */
    Stream<? extends ModEntry> getInstalledMods();
}
