package com.sollace.fabwork.api;

import java.util.Locale;
import java.util.Objects;

/**
 * An installation requirement used to indicate in which environments a mod needs to be available
 * on to allow client-server interplay.
 *
 * Default for all mods that don't provide their own requirement value is "NONE"
 *
 * @author Sollace
 */
public enum RequirementType {
    /**
     * Default
     *
     * No requirement. Connection is allowed to continue regardless of whether the mod
     * is present on either the client or server.
     */
    NONE,
    /**
     * Mod is required only on the client
     */
    CLIENT,
    /**
     * Mod is required only on the server
     */
    SERVER,
    /**
     * Mod is required on both client and server
     */
    BOTH;

    public boolean requiredOnEither() {
        return this != RequirementType.NONE;
    }

    public boolean isRequiredOnServer() {
        return this == BOTH || this == SERVER;
    }

    public boolean isRequiredOnClient() {
        return this == BOTH || this == CLIENT;
    }

    public boolean supercedes(RequirementType requirement) {
        return ordinal() > requirement.ordinal();
    }

    public static RequirementType forKey(String key) {
        return switch (Objects.requireNonNullElse(key, "").toLowerCase(Locale.ROOT).trim()) {
                case "*" -> BOTH;
                case "client" -> CLIENT;
                case "server" -> SERVER;
                default -> NONE;
        };
    }
}
