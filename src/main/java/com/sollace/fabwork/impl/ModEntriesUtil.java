package com.sollace.fabwork.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sollace.fabwork.api.ModEntry;

public interface ModEntriesUtil {

    static Set<String> compare(Stream<ModEntryImpl> provided, List<ModEntryImpl> required) {
        return provided
                .map(ModEntry::modId)
                .filter(id -> required.stream().filter(cc -> cc.modId().equalsIgnoreCase(id)).findAny().isEmpty())
                .distinct()
                .collect(Collectors.toSet());
    }

    static Set<String> ids(List<ModEntryImpl> entries) {
        return entries.stream().map(ModEntry::modId).distinct().collect(Collectors.toSet());
    }

    static String stringify(List<ModEntryImpl> entries) {
        String[] values = entries.stream().map(ModEntry::modId).toArray(String[]::new);
        return " [" + String.join(", ", values) + "] (" + values.length + ")";
    }
}
