package com.sollace.fabwork.impl;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public interface ClientConnectionAccessor {
    ClientConnection getConnection();

    static ClientConnection get(ServerPlayNetworkHandler handler) {
        return ((ClientConnectionAccessor)handler).getConnection();
    }
}
