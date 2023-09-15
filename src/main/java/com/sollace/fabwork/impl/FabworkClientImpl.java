package com.sollace.fabwork.impl;

import com.sollace.fabwork.api.client.ModProvisionCallback;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sollace.fabwork.api.client.FabworkClient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;

public class FabworkClientImpl implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("Fabwork::CLIENT");
    private static final SynchronisationState EMPTY_STATE = new SynchronisationState(FabworkImpl.INSTANCE.getInstalledMods(), Stream.empty());

    private static SynchronisationState STATE = EMPTY_STATE;
    public static final FabworkClient INSTANCE = () -> STATE.installedOnServer().stream();

    @Override
    public void onInitializeClient() {
        if (Debug.NO_CLIENT) {
            return;
        }

        if (!FabworkConfig.INSTANCE.get().disableLoginProtocol) {

            ClientConfigurationConnectionEvents.INIT.register((handler, client) -> {
                LoaderUtil.invokeUntrusted(() -> {
                    STATE.installedOnServer().forEach(entry -> {
                        ModProvisionCallback.EVENT.invoker().onModProvisioned(entry, false);
                    });
                    STATE = EMPTY_STATE;
                }, "Client connection init");
            });
            ClientConfigurationNetworking.registerGlobalReceiver(FabworkServer.CONSENT_ID, (client, handler, buffer, response) -> {
                LoaderUtil.invokeUntrusted(() -> {
                    STATE = new SynchronisationState(FabworkImpl.INSTANCE.getInstalledMods(), ModEntryImpl.read(buffer));
                    LOGGER.info("Got mod list from server: {}", ModEntriesUtil.stringify(STATE.installedOnServer()));
                    Set<String> serverModIds = STATE.installedOnServer().stream().map(ModEntryImpl::modId).distinct().collect(Collectors.toSet());
                    response.sendPacket(FabworkServer.CONSENT_ID, ModEntryImpl.write(
                            FabworkImpl.INSTANCE.getInstalledMods().filter(entry -> entry.requiredOnEither() || serverModIds.contains(entry.modId())),
                            PacketByteBufs.create())
                    );
                }, "Responding to server sync packet");
            });

            ClientConfigurationConnectionEvents.READY.register((handler, client) -> {
                LoaderUtil.invokeUntrusted(() -> {
                    STATE.verify(LOGGER, true).ifPresent(disconnectReason -> {
                       handler.onDisconnect(new DisconnectS2CPacket(disconnectReason));
                    });
                }, "Entering play state");
            });
        }
        LoaderUtil.invokeEntryPoints("fabwork:client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);

        LOGGER.info("Loaded Fabwork {}", FabricLoader.getInstance().getModContainer("fabwork").get().getMetadata().getVersion().getFriendlyString());
    }
}
