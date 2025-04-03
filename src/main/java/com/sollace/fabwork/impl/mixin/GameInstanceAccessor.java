package com.sollace.fabwork.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.GameInstance;
import net.minecraft.server.network.ServerCommonNetworkHandler;

@Mixin(ServerCommonNetworkHandler.class)
public interface GameInstanceAccessor {
    @Accessor("field_58305")
    GameInstance getGameInstance();
}
