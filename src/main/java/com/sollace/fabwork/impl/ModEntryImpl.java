package com.sollace.fabwork.impl;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.sollace.fabwork.api.ModEntry;
import com.sollace.fabwork.api.RequirementType;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.PacketByteBuf;

record ModEntryImpl(
        String modId, RequirementType requirement) implements ModEntry {

    public ModEntryImpl(ModContainer mod) {
        this(mod.getMetadata().getId(), FabworkImpl.getRequirementFor(mod));
    }

    public ModEntryImpl(PacketByteBuf buffer) {
        this(buffer.readString(), buffer.readEnumConstant(RequirementType.class));
    }

    public boolean requiredOnEither() {
        return requirement().requiredOnEither();
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeString(modId);
        buffer.writeEnumConstant(requirement);
    }

    public static PacketByteBuf write(Stream<ModEntryImpl> entries, PacketByteBuf buffer) {
        buffer.writeInt(FabworkServer.PROTOCOL_VERSION);
        buffer.writeCollection(entries.toList(), (c, r) -> r.write(c));
        return buffer;
    }

    public static Stream<ModEntryImpl> read(PacketByteBuf buffer) {
        Preconditions.checkState(buffer.readInt() == FabworkServer.PROTOCOL_VERSION, "Wrong protocol");
        return buffer.readCollection(ArrayList::new, ModEntryImpl::new).stream();
    }
}
