package com.sollace.fabwork.impl;

import com.sollace.fabwork.api.ModEntry;
import com.sollace.fabwork.api.RequirementType;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

record ModEntryImpl(String modId, RequirementType requirement) implements ModEntry {
    public static final StreamCodec<FriendlyByteBuf, ModEntryImpl> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ModEntryImpl::modId,
            RequirementType.STREAM_CODEC, ModEntryImpl::requirement,
            ModEntryImpl::new
    );

    public ModEntryImpl(ModContainer mod) {
        this(mod.getMetadata().getId(), FabworkImpl.getRequirementFor(mod));
    }

    public boolean requiredOnEither() {
        return requirement().requiredOnEither();
    }
}
