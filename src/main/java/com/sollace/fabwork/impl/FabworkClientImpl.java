package com.sollace.fabwork.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sollace.fabwork.api.ModEntry;
import com.sollace.fabwork.api.client.ModProvisionCallback;
import com.sollace.fabwork.api.client.FabworkClient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;

public class FabworkClientImpl implements ClientModInitializer {
    private static SynchronisationState STATE = SynchronisationState.EMPTY;
    public static final FabworkClient INSTANCE = () -> STATE.installedOnServer.stream();

    @Override
    public void onInitializeClient() {
        ClientLoginConnectionEvents.INIT.register((handler, client) -> {
            STATE.installedOnServer.forEach(entry -> {
                ModProvisionCallback.EVENT.invoker().onModProvisioned(entry, false);
            });
            STATE = SynchronisationState.EMPTY;
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            STATE.verify(handler.getConnection());
        });
        ClientPlayNetworking.registerGlobalReceiver(FabworkServer.CONSENT_ID, (client, ignore1, buffer, ignore2) -> {
            STATE = new SynchronisationState(ModEntryImpl.read(buffer));
        });
    }

    record SynchronisationState(
            List<ModEntryImpl> installedOnClient,
            List<ModEntryImpl> installedOnServer) {
        static final SynchronisationState EMPTY = new SynchronisationState(Stream.empty());

        public SynchronisationState(Stream<ModEntryImpl> installedOnServer) {
            this(FabworkImpl.INSTANCE.getInstalledMods().toList(), installedOnServer.toList());
        }

        public void verify(ClientConnection connection) {
            Set<String> missingOnServer = getDifference(installedOnClient.stream().filter(c -> c.requirement().isRequiredOnServer()), installedOnServer);
            Set<String> missingOnClient = getDifference(installedOnServer.stream().filter(c -> c.requirement().isRequiredOnClient()), installedOnClient);

            installedOnServer.stream().forEach(entry -> {
                ModProvisionCallback.EVENT.invoker().onModProvisioned(entry, !missingOnClient.contains(entry.modId()));
            });

            if (!missingOnServer.isEmpty() || !missingOnClient.isEmpty()) {
                Text errorMessage = createErrorMessage(missingOnServer, missingOnClient);
                FabworkServer.LOGGER.error(errorMessage.getString());
                connection.disconnect(errorMessage);
            } else {
                String[] installed = installedOnServer.stream().map(ModEntry::modId).toArray(String[]::new);

                FabworkServer.LOGGER.info("Joining server with {} syncronised mod(s) [{}]", installed.length, String.join(", ", installed));
            }
        }

        private Set<String> getDifference(Stream<ModEntryImpl> provided, List<ModEntryImpl> required) {
            return provided
                    .map(ModEntry::modId)
                    .filter(id -> required.stream().filter(cc -> cc.modId().equalsIgnoreCase(id)).findAny().isEmpty())
                    .distinct()
                    .collect(Collectors.toSet());
        }

        private Text createErrorMessage(Set<String> missingOnServer, Set<String> missingOnClient) {
            String serverMissing = String.join(", ", missingOnServer.stream().toArray(CharSequence[]::new));
            String clientMissing = String.join(", ", missingOnClient.stream().toArray(CharSequence[]::new));

            if (missingOnClient.isEmpty()) {
                return Text.translatable("fabwork.error.server_missing_mods", serverMissing);
            }

            if (missingOnServer.isEmpty()) {
                return Text.translatable("fabwork.error.client_missing_mods", clientMissing);
            }

            return Text.translatable("fabwork.error.both_missing_mods", clientMissing, serverMissing);
        }
    }
}
