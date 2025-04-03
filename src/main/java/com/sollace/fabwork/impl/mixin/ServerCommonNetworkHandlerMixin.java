package com.sollace.fabwork.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sollace.fabwork.impl.ClientConnectionAccessor;
import com.sollace.fabwork.impl.PlayPingSynchroniser;

import net.minecraft.class_10972;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;

@Mixin(class_10972.class)
abstract class ServerCommonNetworkHandlerMixin implements ServerCommonPacketListener, ClientConnectionAccessor {
    @Inject(method = "onPong(Lnet/minecraft/network/packet/c2s/common/CommonPongC2SPacket;)V", at = @At("HEAD"))
    private void onOnPong(CommonPongC2SPacket packet, CallbackInfo info) {
        Object self = this;
        if (self instanceof GameInstanceAccessor handler) {
            NetworkThreadUtils.forceMainThread(packet, this, handler.getGameInstance().getThreadExecutor());
            PlayPingSynchroniser.onClientResponse(packet, handler.getGameInstance().getThreadExecutor());
        }
    }

    @Override
    @Accessor("field_58317")
    public abstract ClientConnection getConnection();
}
