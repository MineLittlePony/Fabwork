package com.sollace.fabwork.api;

/**
 * Represents a mod (identified by its string id) and a installation requirement.
 */
public interface ModEntry {
    String modId();

    RequirementType requirement();
}
