package com.sollace.fabwork.impl;

import com.sollace.fabwork.api.client.ModProvisionCallback;

import java.util.concurrent.*;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sollace.fabwork.api.client.FabworkClient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;

public class FabworkClientImpl implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("Fabwork::CLIENT");
    private static final SynchronisationState EMPTY_STATE = new SynchronisationState(FabworkImpl.INSTANCE.getInstalledMods(), Stream.empty());

    private static SynchronisationState STATE = EMPTY_STATE;
    public static final FabworkClient INSTANCE = () -> STATE.installedOnServer().stream();

    private static final Executor WAITER = CompletableFuture.delayedExecutor(300, TimeUnit.MILLISECONDS);

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            LOGGER.info("Client provisioned new connection " + handler.hashCode());
            STATE.installedOnServer().forEach(entry -> {
                ModProvisionCallback.EVENT.invoker().onModProvisioned(entry, false);
            });
            STATE = EMPTY_STATE;
        });
        ClientPlayNetworking.registerGlobalReceiver(FabworkServer.CONSENT_ID, (client, handler, buffer, response) -> {
            STATE = new SynchronisationState(FabworkImpl.INSTANCE.getInstalledMods(), ModEntryImpl.read(buffer));
            LOGGER.info("Responding to server sync packet " + handler.hashCode());
            response.sendPacket(FabworkServer.CONSENT_ID, ModEntryImpl.write(
                    FabworkImpl.INSTANCE.getInstalledMods().filter(ModEntryImpl::requiredOnEither),
                    PacketByteBufs.create())
            );
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.info("Entered play state. Server has 300ms to respond " + handler.hashCode());
            CompletableFuture.runAsync(() -> {
                LOGGER.info("Performing verify of server's installed mods " + handler.hashCode());
                STATE.verify(handler.getConnection(), LOGGER, true);
            }, WAITER);
        });
        LoaderUtil.invokeEntryPoints("fabwork:client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);

        LOGGER.info("Loaded Fabwork " + FabricLoader.getInstance().getModContainer("fabwork").get().getMetadata().getVersion().getFriendlyString());
    }
}
