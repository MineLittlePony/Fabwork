package com.sollace.fabwork.impl;

import java.util.List;

import org.spongepowered.include.com.google.common.base.Preconditions;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConsentMessage(int protocolVersion, List<ModEntryImpl> entries) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConsentMessage> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("fabwork", "synchronize"));
    public static final StreamCodec<FriendlyByteBuf, ConsentMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ConsentMessage::protocolVersion,
            ModEntryImpl.STREAM_CODEC.apply(ByteBufCodecs.list()), ConsentMessage::entries,
            ConsentMessage::new
    );

    public ConsentMessage(List<ModEntryImpl> entries) {
        this(FabworkServer.PROTOCOL_VERSION, entries);
    }

    public ConsentMessage {
        Preconditions.checkState(protocolVersion == FabworkServer.PROTOCOL_VERSION, "Wrong protocol: Got " + protocolVersion + " but " + FabworkServer.PROTOCOL_VERSION + " was expected.");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
