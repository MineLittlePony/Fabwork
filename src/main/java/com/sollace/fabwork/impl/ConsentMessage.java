package com.sollace.fabwork.impl;

import java.util.List;

import org.spongepowered.include.com.google.common.base.Preconditions;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConsentMessage(List<ModEntryImpl> entries) implements CustomPayload {
    public static final CustomPayload.Id<ConsentMessage> ID = new CustomPayload.Id<>(Identifier.of("fabwork", "synchronize"));
    public static final PacketCodec<PacketByteBuf, ConsentMessage> CODEC = CustomPayload.codecOf(
            (message, buffer) -> {
                buffer.writeInt(FabworkServer.PROTOCOL_VERSION);
                ModEntryImpl.LIST_CODEC.encode(buffer, message.entries());
            },
            buffer -> {
                Preconditions.checkState(buffer.readInt() == FabworkServer.PROTOCOL_VERSION, "Wrong protocol");
                return new ConsentMessage(ModEntryImpl.LIST_CODEC.decode(buffer));
            }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
