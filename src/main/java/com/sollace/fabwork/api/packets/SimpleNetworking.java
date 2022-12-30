package com.sollace.fabwork.api.packets;

import java.util.function.Function;

import com.sollace.fabwork.impl.ReceiverImpl;
import com.sollace.fabwork.impl.packets.ClientSimpleNetworkingImpl;
import com.sollace.fabwork.impl.packets.ServerSimpleNetworkingImpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A side-agnostic, and declaritive wrapper around {@link ServerPlayNetworking} and {@link ClientPlayNetworking}
 * designed to bring networking in line with the declaritive/registered nature of other parts of Mojang's ecosystem.
 * <p>
 * It is safe to call these methods from either the client or the server, so modders can implement a
 * single static <code>PacketTypes</code> class with which they can easily send packets without worrying
 * about the complexities of the network thread, which side to register a global receiver on,
 * which method to use to send, or even whether their receiver is registered for a given player or not.
 * <p>
 * All of the above is handled in a black-box style by this class.
 * <p>
 * <ul>
 * <li>Packets are automatically registered on the appropriate sides.</li>
 * <li>Sending is done in the same way by calling `send` on your packet type.</li>
 * <li>Your packet's <code>handle</code> method is executed on the main thread where it is safe to interact with the world.</li>
 */
public interface SimpleNetworking {
    /**
     * Registers a packet type for transmisison to the server.
     * <p>
     * The returned handle can be used by the client to send messages to the active minecraft server.
     * <p>
     *
     * @param <T>     The type of packet to implement
     * @param id      The message's unique used for serialization
     * @param factory A constructor returning new instances of the packet type
     *
     * @return A registered PacketType
     */
    static <T extends Packet<ServerPlayerEntity>> C2SPacketType<T> clientToServer(Identifier id, Function<PacketByteBuf, T> factory) {
        return ServerSimpleNetworkingImpl.register(id, factory);
    }
    /**
     * Registers a packet type for transmission to the client.
     *
     * The returned handle can be used by the server to send messages to a given recipient.
     *
     * @param <T>     The type of packet to implement
     * @param id      The message's unique used for serialization
     * @param factory A constructor returning new instances of the packet type
     *
     * @return A registered PacketType
     */
    static <T extends Packet<PlayerEntity>> S2CPacketType<T> serverToClient(Identifier id, Function<PacketByteBuf, T> factory) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return ClientSimpleNetworkingImpl.register(id, factory);
        }
        return new S2CPacketType<>(id, factory, ReceiverImpl.empty(id));
    }
}
