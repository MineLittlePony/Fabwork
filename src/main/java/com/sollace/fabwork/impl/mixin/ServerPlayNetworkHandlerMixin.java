package com.sollace.fabwork.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sollace.fabwork.impl.PlayPingSynchroniser;

import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerPlayNetworkHandlerMixin implements ServerPlayPacketListener {
    @Inject(method = "onPong(Lnet/minecraft/network/packet/c2s/play/PlayPongC2SPacket;)V", at = @At("HEAD"))
    private void onOnPong(PlayPongC2SPacket packet, CallbackInfo info) {
        NetworkThreadUtils.forceMainThread(packet, this, ((ServerPlayNetworkHandler)(Object)this).player.getWorld());
        PlayPingSynchroniser.onClientResponse(packet, ((ServerPlayNetworkHandler)(Object)this).player.getWorld().getServer());
    }
}
