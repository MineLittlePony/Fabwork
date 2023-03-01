package com.sollace.fabwork.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sollace.fabwork.impl.event.ServerConnectionEvents;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/SynchronizeTagsS2CPacket;<init>(Ljava/util/Map;)V"))
    private void handlePlayerConnection(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        ServerConnectionEvents.CONNECT.invoker().onPlayReady(player.networkHandler, ServerPlayNetworking.getSender(player), player.getServer());
    }
}