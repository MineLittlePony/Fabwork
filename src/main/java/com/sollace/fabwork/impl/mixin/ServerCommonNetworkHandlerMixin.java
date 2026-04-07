package com.sollace.fabwork.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sollace.fabwork.impl.ClientConnectionAccessor;
import com.sollace.fabwork.impl.PlayPingSynchroniser;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@Mixin(ServerCommonPacketListenerImpl.class)
abstract class ServerCommonNetworkHandlerMixin implements ServerCommonPacketListener, ClientConnectionAccessor {

    @Shadow
    protected @Final MinecraftServer server;

    @Inject(method = "handlePong", at = @At("HEAD"))
    private void onOnPong(ServerboundPongPacket packet, CallbackInfo info) {
        PacketUtils.ensureRunningOnSameThread(packet, this, server.packetProcessor());
        PlayPingSynchroniser.onClientResponse(packet, server);
    }

    @Override
    @Accessor("connection")
    public abstract Connection getConnection();
}
