package com.sollace.fabwork.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Streams;
import com.sollace.fabwork.api.Fabwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerConfigurationTask;

public class FabworkServer implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Fabwork::SERVER");
    public static final ServerPlayerConfigurationTask.Key MOD_LIST_SYNC_TASK = new ServerPlayerConfigurationTask.Key(ConsentMessage.ID.id().toString());
    public static final int PROTOCOL_VERSION = 1;

    public static final Fabwork FABWORK = FabworkImpl.INSTANCE;

    @Override
    public void onInitialize() {
        if (Debug.NO_SERVER) {
            return;
        }

        final FabworkConfig config = FabworkConfig.INSTANCE.get();
        final SynchronisationState emptyState = new SynchronisationState(Stream.empty(),
                makeDistinct(Streams.concat(FabworkImpl.INSTANCE.getInstalledMods().filter(ModEntryImpl::requiredOnEither), config.getCustomRequiredMods()))
        );

        if (!config.disableLoginProtocol) {
            ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
                ClientConnection connection = ClientConnectionAccessor.get(handler);

                if (ServerConfigurationNetworking.canSend(handler, ConsentMessage.ID)) {
                    handler.addTask(new ServerPlayerConfigurationTask() {
                        @Override
                        public void sendPacket(Consumer<Packet<?>> sender) {
                            LOGGER.info("Sending mod list to {}[{}]", handler.getDebugProfile().getName(), connection.getAddress());
                            sender.accept(ServerConfigurationNetworking.createS2CPacket(new ConsentMessage(emptyState.installedOnServer())));
                        }

                        @Override
                        public Key getKey() {
                            return MOD_LIST_SYNC_TASK;
                        }
                    });
                } else {
                    LOGGER.warn("{}[{}] does not appear to have fabwork installed", handler.getDebugProfile().getName(), connection.getAddress());
                    if (config.allowUnmoddedClients) {
                        LOGGER.warn("Connection to {}[{}] has been force permitted by server configuration. They are allowed to join checking installed mods! Their game may be broken upon joining!", handler.getDebugProfile().getName(), connection.getAddress());
                    } else {
                        emptyState.verify(LOGGER, false).ifPresent(handler::disconnect);
                    }
                }
            });

            PayloadTypeRegistry.configurationC2S().register(ConsentMessage.ID, ConsentMessage.CODEC);
            ServerConfigurationNetworking.registerGlobalReceiver(ConsentMessage.ID, (payload, context) -> {
                LoaderUtil.invokeUntrusted(() -> {
                    SynchronisationState state = new SynchronisationState(payload.entries().stream(), emptyState.installedOnServer().stream());
                    ClientConnection connection = ClientConnectionAccessor.get(context.networkHandler());
                    LOGGER.info("Got mod list from {}[{}]: {}", context.networkHandler().getDebugProfile().getName(), connection.getAddress(), ModEntriesUtil.stringify(state.installedOnClient()));
                    state.verify(LOGGER, true).ifPresentOrElse(context.networkHandler()::disconnect, () -> context.networkHandler().completeTask(MOD_LIST_SYNC_TASK));
                }, "Received synchronize response from client");
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
}
