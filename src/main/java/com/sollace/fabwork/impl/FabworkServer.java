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
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;

public class FabworkServer implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Fabwork::SERVER");
    public static final ConfigurationTask.Type MOD_LIST_SYNC_TASK = new ConfigurationTask.Type(ConsentMessage.ID.id().toString());
    public static final int PROTOCOL_VERSION = 1;

    public static final Fabwork FABWORK = FabworkImpl.INSTANCE;

    @Override
    public void onInitialize() {
        if (Debug.NO_SERVER) {
            return;
        }

        PayloadTypeRegistry.clientboundConfiguration().register(ConsentMessage.ID, ConsentMessage.STREAM_CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(ConsentMessage.ID, ConsentMessage.STREAM_CODEC);

        final FabworkConfig config = FabworkConfig.INSTANCE.get();
        final SynchronisationState emptyState = new SynchronisationState(Stream.empty(),
                makeDistinct(Streams.concat(FabworkImpl.INSTANCE.getInstalledMods().filter(ModEntryImpl::requiredOnEither), config.getCustomRequiredMods()))
        );

        if (!config.disableLoginProtocol) {
            ServerConfigurationConnectionEvents.CONFIGURE.register((handler, _) -> {
                Connection connection = ClientConnectionAccessor.get(handler);

                if (ServerConfigurationNetworking.canSend(handler, ConsentMessage.ID)) {
                    handler.addTask(new ConfigurationTask() {
                        @Override
                        public void start(Consumer<Packet<?>> sender) {
                            LOGGER.info("Sending mod list to {}[{}]", handler.getOwner().name(), connection.getLoggableAddress(true));
                            sender.accept(ServerConfigurationNetworking.createClientboundPacket(new ConsentMessage(emptyState.installedOnServer())));
                        }

                        @Override
                        public Type type() {
                            return MOD_LIST_SYNC_TASK;
                        }
                    });
                } else {
                    LOGGER.warn("{}[{}] does not appear to have fabwork installed", handler.getOwner().name(), connection.getLoggableAddress(true));
                    if (config.allowUnmoddedClients) {
                        LOGGER.warn("Connection to {}[{}] has been force permitted by server configuration. They are allowed to join checking installed mods! Their game may be broken upon joining!", handler.getOwner().name(), connection.getLoggableAddress(true));
                    } else {
                        emptyState.verify(LOGGER, false).ifPresent(handler::disconnect);
                    }
                }
            });

            ServerConfigurationNetworking.registerGlobalReceiver(ConsentMessage.ID, (payload, context) -> {
                LoaderUtil.invokeUntrusted(() -> {
                    SynchronisationState state = new SynchronisationState(payload.entries().stream(), emptyState.installedOnServer().stream());
                    Connection connection = ClientConnectionAccessor.get(context.packetListener());
                    LOGGER.info("Got mod list from {}[{}]: {}", context.packetListener().getOwner().name(), connection.getLoggableAddress(true), ModEntriesUtil.stringify(state.installedOnClient()));
                    state.verify(LOGGER, true).ifPresentOrElse(context.packetListener()::disconnect, () -> context.packetListener().completeTask(MOD_LIST_SYNC_TASK));
                }, "Received synchronize response from client");
            });
        }
        LoaderUtil.invokeEntryPoints("fabwork:main", ModInitializer.class, ModInitializer::onInitialize);

        LOGGER.info("Loaded Fabwork " + FabricLoader.getInstance().getModContainer("fabwork").get().getMetadata().getVersion().getFriendlyString());
    }

    private static Stream<ModEntryImpl> makeDistinct(Stream<ModEntryImpl> entries) {
        Map<String, ModEntryImpl> map = new HashMap<>();
        entries.forEach(entry -> {
            map.compute(entry.modId(), (_, value) -> value == null || entry.requirement().supercedes(value.requirement()) ? entry : value);
        });
        return map.values().stream();
    }
}
