package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.intercompatibility.MinecraftCompatibilityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class MinecraftServerImpl implements MinecraftServer {

    protected net.minecraft.server.MinecraftServer server;

    public MinecraftServerImpl(net.minecraft.server.MinecraftServer server) {
        this.server = server;
    }

    public net.minecraft.server.MinecraftServer getMinecraftServer() {
        return server;
    }

    @Override
    public int getPort() {
        return server.getPort();
    }

    @Override
    public String getIp() {
        return server.getLocalIp();
    }

    @Override
    public ServerPlayer getPlayer(UUID playerUUID) {
        return MinecraftCompatibilityManager.fromServerPlayer(server.getPlayerList().getPlayer(playerUUID));
    }

    @Override
    public List<ServerPlayer> getPlayers() {
        return server.getPlayerList().getPlayers().stream().map(MinecraftCompatibilityManager::fromServerPlayer).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean isDedicated() {
        return server.isDedicatedServer();
    }

    @Override
    public boolean usesAuthentication() {
        return server.usesAuthentication();
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return server.getOperatorUserPermissionLevel();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        MinecraftServerImpl that = (MinecraftServerImpl) object;
        return Objects.equals(server, that.server);
    }

    @Override
    public int hashCode() {
        return server != null ? server.hashCode() : 0;
    }
}
