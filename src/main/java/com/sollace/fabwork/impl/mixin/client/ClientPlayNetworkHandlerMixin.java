package com.sollace.fabwork.impl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sollace.fabwork.impl.event.ClientConnectionEvents;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;

@Mixin(value = ClientPlayNetworkHandler.class, priority = 999)
abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onSynchronizeRecipes", at = @At("RETURN"))
    private void onOnSynchronizeRecipes(SynchronizeRecipesS2CPacket packet, CallbackInfo cinfo) {
        final MinecraftClient client = MinecraftClient.getInstance();
        ClientConnectionEvents.CONNECT.invoker().onPlayReady(client.player.networkHandler, ClientPlayNetworking.getSender(), client);
    }
}
