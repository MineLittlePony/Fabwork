package com.sollace.fabwork.impl;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;

public interface ClientConnectionAccessor {
    ClientConnection getConnection();

    static ClientConnection get(ServerCommonNetworkHandler handler) {
        return ((ClientConnectionAccessor)handler).getConnection();
    }
}
