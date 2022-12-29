package com.sollace.fabwork.api;

import java.util.Locale;

public enum RequirementType {
    NONE,
    CLIENT,
    SERVER,
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
        if (key == null) {
            return NONE;
        }
        key = key.toLowerCase(Locale.ROOT).trim();
        switch (key) {
            case "*": return BOTH;
            case "client": return CLIENT;
            case "server": return SERVER;
            default: return NONE;
        }
    }
}
