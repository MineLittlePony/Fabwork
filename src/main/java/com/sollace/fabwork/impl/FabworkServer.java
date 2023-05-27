package com.sollace.fabwork.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Streams;
import com.sollace.fabwork.api.Fabwork;
import com.sollace.fabwork.impl.PlayPingSynchroniser.ResponseType;
import com.sollace.fabwork.impl.event.ServerConnectionEvents;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;

public class FabworkServer implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Fabwork::SERVER");
    public static final Identifier CONSENT_ID = id("synchronize");
    public static final int PROTOCOL_VERSION = 1;

    public static final Fabwork FABWORK = FabworkImpl.INSTANCE;

    @Override
    public void onInitialize() {
        final FabworkConfig config = FabworkConfig.INSTANCE.get();
        final Map<ClientConnection, SynchronisationState> clientLoginStates = new HashMap<>();
        final SynchronisationState emptyState = new SynchronisationState(Stream.empty(),
                makeDistinct(Streams.concat(FabworkImpl.INSTANCE.getInstalledMods().filter(ModEntryImpl::requiredOnEither), config.getCustomRequiredMods()))
        );

        if (!config.disableLoginProtocol) {
            ServerPlayNetworking.registerGlobalReceiver(CONSENT_ID, (server, player, handler, buffer, response) -> {
                LoaderUtil.invokeUntrusted(() -> {
                    SynchronisationState state = new SynchronisationState(ModEntryImpl.read(buffer), emptyState.installedOnServer().stream());
                    ClientConnection connection = ClientConnectionAccessor.get(handler);
                    LOGGER.info("Got mod list from {}[{}]: {}", player.getName().getString(), connection.getAddress(), ModEntriesUtil.stringify(state.installedOnClient()));
                    clientLoginStates.put(connection, state);
                }, "Received synchronize response from client");
            });

            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                LoaderUtil.invokeUntrusted(() -> {
                    LOGGER.info("Sending mod list to {}[{}]", handler.getPlayer().getName().getString(), handler.getConnectionAddress());
                    sender.sendPacket(CONSENT_ID, ModEntryImpl.write(
                            emptyState.installedOnServer().stream(),
                            PacketByteBufs.create())
                    );
                }, "Sending synchronize packet");
            });

            ServerConnectionEvents.CONNECT.register((handler, sender, server) -> {
                LoaderUtil.invokeUntrusted(() -> {
                    PlayPingSynchroniser.waitForClientResponse(ClientConnectionAccessor.get(handler), responseType -> {
                        ClientConnection connection = ClientConnectionAccessor.get(handler);
                        if (responseType == ResponseType.COMPLETED) {
                            if (clientLoginStates.containsKey(connection)) {
                                clientLoginStates.remove(connection).verify(connection, LOGGER, true);
                            } else {
                                LOGGER.warn("{}[{}] did not send a mod list. They may not have fabwork installed", handler.getPlayer().getName().getString(), handler.getConnectionAddress());
                                if (config.allowUnmoddedClients) {
                                    LOGGER.warn("Connection to {}[{}] has been force permitted by server configuration. They are allowed to join checking installed mods! Their game may be broken upon joining!", handler.getPlayer().getName().getString(), handler.getConnectionAddress());
                                } else {
                                    emptyState.verify(connection, LOGGER, false);
                                }
                            }
                        } else {
                            LOGGER.warn("Failed to receive response from client. {}[{}] ConnectionState: {}",
                                    handler.getPlayer().getName().getString(),
                                    handler.getConnectionAddress(),
                                    handler.isConnectionOpen() ? " OPEN" : " CLOSED"
                            );
                        }
                    });
                }, "Sending synchronize packet");
            });
        }
        LoaderUtil.invokeEntryPoints("fabwork:main", ModInitializer.class, ModInitializer::onInitialize);

        LOGGER.info("Loaded Fabwork " + FabricLoader.getInstance().getModContainer("fabwork").get().getMetadata().getVersion().getFriendlyString());
    }

    private static Stream<ModEntryImpl> makeDistinct(Stream<ModEntryImpl> entries) {
        Map<String, ModEntryImpl> map = new HashMap<>();
        entries.forEach(entry -> {
            map.compute(entry.modId(), (id, value) -> value == null || entry.requirement().supercedes(value.requirement()) ? entry : value);
        });
        return map.values().stream();
    }

    private static Identifier id(String name) {
        return new Identifier("fabwork", name);
    }
}
