package com.sollace.fabwork.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import com.sollace.fabwork.api.ModEntry;
import com.sollace.fabwork.api.client.ModProvisionCallback;

import net.minecraft.text.Text;

record SynchronisationState(
        List<ModEntryImpl> installedOnClient,
        List<ModEntryImpl> installedOnServer) {

    public SynchronisationState(Stream<ModEntryImpl> installedOnClient, Stream<ModEntryImpl> installedOnServer) {
        this(installedOnClient.toList(), installedOnServer.toList());
    }

    public Optional<Text> verify(Logger logger, boolean useTranslation) {
        Set<String> missingOnServer = ModEntriesUtil.compare(installedOnClient.stream().filter(c -> c.requirement().isRequiredOnServer()), installedOnServer);
        Set<String> missingOnClient = ModEntriesUtil.compare(installedOnServer.stream().filter(c -> c.requirement().isRequiredOnClient()), installedOnClient);

        installedOnServer.stream().forEach(entry -> {
            ModProvisionCallback.EVENT.invoker().onModProvisioned(entry, !missingOnClient.contains(entry.modId()));
        });

        if (!missingOnServer.isEmpty() || !missingOnClient.isEmpty()) {
            Text errorMessage = createErrorMessage(missingOnServer, missingOnClient, useTranslation);
            logger.error(errorMessage.getString());

            if (FabworkConfig.INSTANCE.get().doNotEnforceModMatching) {
                logger.info("Connection would fail with message '{}' but was allowed anyway due to configured rules.", errorMessage.toString());
                return Optional.empty();
            }

            return Optional.of(errorMessage);
        }

        String[] installed = installedOnServer.stream().map(ModEntry::modId).toArray(String[]::new);

        logger.info("Connection succeeded with {} syncronised mod(s) [{}]", installed.length, String.join(", ", installed));
        return Optional.empty();
    }

    private Text createErrorMessage(Set<String> missingOnServer, Set<String> missingOnClient, boolean useTranslation) {
        String serverMissing = String.join(", ", missingOnServer.stream().toArray(CharSequence[]::new));
        String clientMissing = String.join(", ", missingOnClient.stream().toArray(CharSequence[]::new));

        if (missingOnClient.isEmpty()) {
            return Text.translatable(
                    useTranslation ? "fabwork.error.server_missing_mods" : "Server is missing required mod(s). Remove these from your client to join this server. [%s]",
                    serverMissing
            );
        }

        if (missingOnServer.isEmpty()) {
            return Text.translatable(
                    useTranslation ? "fabwork.error.client_missing_mods" : "Client is missing required mod(s). Add these to your client to join this server. [%s]",
                    clientMissing
            );
        }

        return Text.translatable(
                useTranslation ? "fabwork.error.both_missing_mods" : "Client and Server are missing required mod(s). Client needs to install [%s] and remove [%s] in order to join this server.",
                clientMissing, serverMissing
        );
    }
}