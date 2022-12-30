package com.sollace.fabwork.api.client;

import java.util.stream.Stream;

import com.sollace.fabwork.api.ModEntry;
import com.sollace.fabwork.impl.FabworkClientImpl;

/**
*
* Client-Specific methods for working with Fabwork's network requirements API.
* For common client/server methods, see {@link Fabwork}
*
* @author Sollace
*
* @see com.sollace.fabwork.api.Fabwork
*/
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
