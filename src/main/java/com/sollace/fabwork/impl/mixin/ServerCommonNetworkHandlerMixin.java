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

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;

@Mixin(ServerCommonNetworkHandler.class)
abstract class ServerCommonNetworkHandlerMixin implements ServerCommonPacketListener, ClientConnectionAccessor {

    @Shadow
    protected @Final MinecraftServer server;

    @Inject(method = "onPong(Lnet/minecraft/network/packet/c2s/common/CommonPongC2SPacket;)V", at = @At("HEAD"))
    private void onOnPong(CommonPongC2SPacket packet, CallbackInfo info) {
        NetworkThreadUtils.forceMainThread(packet, this, server);
        PlayPingSynchroniser.onClientResponse(packet, server);
    }

    @Override
    @Accessor("connection")
    public abstract ClientConnection getConnection();
}
