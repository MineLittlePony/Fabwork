package com.sollace.fabwork.impl;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;

public interface ClientConnectionAccessor {
    Connection getConnection();

    static Connection get(ServerCommonPacketListener handler) {
        return ((ClientConnectionAccessor)handler).getConnection();
    }
}
