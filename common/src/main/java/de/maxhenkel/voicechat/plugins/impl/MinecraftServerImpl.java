package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.MinecraftServer;
import de.maxhenkel.voicechat.api.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MinecraftServerImpl implements MinecraftServer {

    protected net.minecraft.server.MinecraftServer server;

    public MinecraftServerImpl(net.minecraft.server.MinecraftServer server) {
        this.server = server;
    }

    public Object getMinecraftServer() {
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
        return Voicechat.outSourcing.getServerApi().fromServerPlayer(server.getPlayerList().getPlayer(playerUUID));
    }

    @Override
    public List<ServerPlayer> getPlayers() {
        return server.getPlayerList().getPlayers().stream().map(player -> Voicechat.outSourcing.getServerApi().fromServerPlayer(player)).collect(Collectors.toCollection(ArrayList::new));
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
}
