package com.sollace.fabwork.impl;

import java.util.List;
import com.sollace.fabwork.api.ModEntry;
import com.sollace.fabwork.api.RequirementType;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

record ModEntryImpl(String modId, RequirementType requirement) implements ModEntry {
    public static final PacketCodec<PacketByteBuf, List<ModEntryImpl>> LIST_CODEC = PacketCodec.<PacketByteBuf, ModEntryImpl>of((entry, buffer) -> {
        buffer.writeString(entry.modId());
        buffer.writeEnumConstant(entry.requirement());
    },
        buffer -> new ModEntryImpl(buffer.readString(), buffer.readEnumConstant(RequirementType.class))
    ).collect(PacketCodecs.toList());

    public ModEntryImpl(ModContainer mod) {
        this(mod.getMetadata().getId(), FabworkImpl.getRequirementFor(mod));
    }

    public boolean requiredOnEither() {
        return requirement().requiredOnEither();
    }
}
