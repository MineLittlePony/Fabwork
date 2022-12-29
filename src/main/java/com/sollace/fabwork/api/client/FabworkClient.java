package com.sollace.fabwork.api.client;

import java.util.stream.Stream;

import com.sollace.fabwork.api.ModEntry;
import com.sollace.fabwork.impl.FabworkClientImpl;

public interface FabworkClient {
    FabworkClient INSTANCE = FabworkClientImpl.INSTANCE;

    /**
     * Gets a stream of mods reported to be installed on the server.
     * <p>
     * If the player is not currently joined to a server, or the server has not reported their installed mods,
     * will return an empty stream.
     *
     * @return Stream of mod entries
     */
    Stream<? extends ModEntry> getServerInstalledMods();
}
