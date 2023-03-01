package com.sollace.fabwork.impl;

import com.sollace.fabwork.api.client.ModProvisionCallback;
import com.sollace.fabwork.impl.event.ClientConnectionEvents;

import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sollace.fabwork.api.client.FabworkClient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public class FabworkClientImpl implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("Fabwork::CLIENT");
    private static final SynchronisationState EMPTY_STATE = new SynchronisationState(FabworkImpl.INSTANCE.getInstalledMods(), Stream.empty());

    private static SynchronisationState STATE = EMPTY_STATE;
    public static final FabworkClient INSTANCE = () -> STATE.installedOnServer().stream();

    private static final int MAX_RETRIES = 5;
    private static final long VERIFY_DELAY = 300;

    private static final Executor WAITER = CompletableFuture.delayedExecutor(VERIFY_DELAY, TimeUnit.MILLISECONDS);

    @Override
    public void onInitializeClient() {
        if (!FabworkConfig.INSTANCE.get().disableLoginProtocol) {
            ClientPlayConnectionEvents.INIT.register((handler, client) -> {
                LoaderUtil.invokeUntrusted(() -> {
                    STATE.installedOnServer().forEach(entry -> {
                        ModProvisionCallback.EVENT.invoker().onModProvisioned(entry, false);
                    });
                    STATE = EMPTY_STATE;
                }, "Client connection init");

            });
            ClientPlayNetworking.registerGlobalReceiver(FabworkServer.CONSENT_ID, (client, handler, buffer, response) -> {
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
            ClientConnectionEvents.CONNECT.register((handler, sender, client) -> {
                LoaderUtil.invokeUntrusted(() -> delayVerify(handler, MAX_RETRIES), "Entering play state");
            });
        }
        LoaderUtil.invokeEntryPoints("fabwork:client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);

        LOGGER.info("Loaded Fabwork {}", FabricLoader.getInstance().getModContainer("fabwork").get().getMetadata().getVersion().getFriendlyString());
    }

    private void delayVerify(ClientPlayNetworkHandler handler, int retries) {
        CompletableFuture.runAsync(() -> {
            LoaderUtil.invokeUntrusted(() -> {
                if (STATE == EMPTY_STATE && retries > 0) {
                    LOGGER.info("Server has not responded. Retrying ({}/{})", (MAX_RETRIES - retries) + 1, MAX_RETRIES);
                    delayVerify(handler, retries - 1);
                } else {
                    STATE.verify(handler.getConnection(), LOGGER, true);
                }
            }, "Verifying host mods retry=" + (MAX_RETRIES - retries));
        }, WAITER);
    }
}
