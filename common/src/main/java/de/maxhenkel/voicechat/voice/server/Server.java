package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.api.VoicechatSocket;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {

    private final Map<UUID, ClientConnection> connections;
    private final Map<UUID, UUID> secrets;
    private final int port;
    private final MinecraftServer server;
    private final VoicechatSocket socket;
    private final ProcessThread processThread;
    private final BlockingQueue<RawUdpPacket> packetQueue;
    private final PingManager pingManager;
    private final PlayerStateManager playerStateManager;
    private final GroupManager groupManager;

    public Server(MinecraftServer server) {
        if (server instanceof DedicatedServer) {
            int configPort = Voicechat.SERVER_CONFIG.voiceChatPort.get();
            if (configPort < 0) {
                Voicechat.LOGGER.info("Using the Minecraft servers port as voice chat port");
                port = server.getPort();
            } else {
                port = configPort;
            }
        } else {
            port = 0;
        }
        this.server = server;
        socket = PluginManager.instance().getSocketImplementation(server);
        connections = new HashMap<>();
        secrets = new HashMap<>();
        packetQueue = new LinkedBlockingQueue<>();
        pingManager = new PingManager(this);
        playerStateManager = new PlayerStateManager();
        groupManager = new GroupManager(this);
        setDaemon(true);
        setName("VoiceChatServerThread");
        processThread = new ProcessThread();
        processThread.start();
    }

    @Override
    public void run() {
        try {
            socket.open(port, Voicechat.SERVER_CONFIG.voiceChatBindAddress.get());
            Voicechat.LOGGER.info("Server started at port {}", socket.getLocalPort());

            while (!socket.isClosed()) {
                try {
                    packetQueue.add(socket.read());
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UUID getSecret(UUID playerUUID) {
        if (hasSecret(playerUUID)) {
            return secrets.get(playerUUID);
        } else {
            UUID secret = UUID.randomUUID();
            secrets.put(playerUUID, secret);
            return secret;
        }
    }

    public boolean hasSecret(UUID playerUUID) {
        return secrets.containsKey(playerUUID);
    }

    public void disconnectClient(UUID playerUUID) {
        connections.remove(playerUUID);
        secrets.remove(playerUUID);
        PluginManager.instance().onPlayerDisconnected(server, playerUUID);
    }

    public void close() {
        socket.close();
        processThread.close();

        PluginManager.instance().onServerStopped(server);
    }

    public boolean isClosed() {
        return !processThread.running;
    }

    private class ProcessThread extends Thread {
        private boolean running;
        private long lastKeepAlive;

        public ProcessThread() {
            running = true;
            lastKeepAlive = 0L;
            setDaemon(true);
            setName("VoiceChatPacketProcessingThread");
        }

        @Override
        public void run() {
            while (running) {
                try {
                    pingManager.checkTimeouts();
                    long keepAliveTime = System.currentTimeMillis();
                    if (keepAliveTime - lastKeepAlive > Voicechat.SERVER_CONFIG.keepAlive.get()) {
                        sendKeepAlives();
                        lastKeepAlive = keepAliveTime;
                    }

                    RawUdpPacket rawPacket = packetQueue.poll(10, TimeUnit.MILLISECONDS);
                    if (rawPacket == null) {
                        continue;
                    }

                    NetworkMessage message;
                    try {
                        message = NetworkMessage.readPacketServer(rawPacket, Server.this);
                    } catch (Exception e) {
                        CooldownTimer.run("failed_reading_packet", () -> {
                            Voicechat.LOGGER.warn("Failed to read packet from {}", rawPacket.getSocketAddress());
                        });
                        continue;
                    }

                    if (System.currentTimeMillis() - message.getTimestamp() > message.getTTL()) {
                        CooldownTimer.run("ttl", () -> {
                            Voicechat.LOGGER.warn("Dropping voice chat packets! Your Server might be overloaded!");
                            Voicechat.LOGGER.warn("Packet queue has {} packets", packetQueue.size());
                        });
                        continue;
                    }

                    if (message.getPacket() instanceof AuthenticatePacket packet) {
                        UUID secret = secrets.get(packet.getPlayerUUID());
                        if (secret != null && secret.equals(packet.getSecret())) {
                            ClientConnection connection;
                            if (!connections.containsKey(packet.getPlayerUUID())) {
                                connection = new ClientConnection(packet.getPlayerUUID(), message.getAddress());
                                connections.put(packet.getPlayerUUID(), connection);
                                Voicechat.LOGGER.info("Successfully authenticated player {}", packet.getPlayerUUID());
                                PluginManager.instance().onPlayerConnected(server.getPlayerList().getPlayer(packet.getPlayerUUID()));
                            } else {
                                connection = connections.get(packet.getPlayerUUID());
                            }
                            sendPacket(new AuthenticateAckPacket(), connection);
                        }
                    }

                    UUID playerUUID = message.getSender(Server.this);
                    if (playerUUID == null) {
                        continue;
                    }

                    ClientConnection conn = connections.get(playerUUID);

                    if (message.getPacket() instanceof MicPacket packet) {
                        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
                        if (player == null) {
                            continue;
                        }
                        if (!PermissionManager.INSTANCE.SPEAK_PERMISSION.hasPermission(player)) {
                            CooldownTimer.run("muted-" + playerUUID, () -> {
                                player.displayClientMessage(new TranslatableComponent("message.voicechat.no_speak_permission"), true);
                            });
                            continue;
                        }
                        PlayerState state = playerStateManager.getState(player.getUUID());
                        if (state == null) {
                            continue;
                        }
                        if (!PluginManager.instance().onMicPacket(player, state, packet)) {
                            processMicPacket(player, state, packet);
                        }
                    } else if (message.getPacket() instanceof PingPacket packet) {
                        pingManager.onPongPacket(packet);
                    } else if (message.getPacket() instanceof KeepAlivePacket) {
                        conn.setLastKeepAliveResponse(System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void close() {
            running = false;
        }
    }

    private void processMicPacket(ServerPlayer player, PlayerState state, MicPacket packet) throws Exception {
        if (state.hasGroup()) {
            processGroupPacket(state, player, packet);
            if (Voicechat.SERVER_CONFIG.openGroups.get()) {
                processProximityPacket(state, player, packet);
            }
            return;
        }
        processProximityPacket(state, player, packet);
    }

    private void processGroupPacket(PlayerState senderState, ServerPlayer sender, MicPacket packet) throws Exception {
        ClientGroup group = senderState.getGroup();
        if (group == null) {
            return;
        }
        GroupSoundPacket groupSoundPacket = new GroupSoundPacket(senderState.getUuid(), packet.getData(), packet.getSequenceNumber());
        NetworkMessage soundMessage = new NetworkMessage(groupSoundPacket);
        for (PlayerState state : playerStateManager.getStates()) {
            if (!group.equals(state.getGroup())) {
                continue;
            }
            if (senderState.getUuid().equals(state.getUuid())) {
                continue;
            }
            ClientConnection connection = connections.get(state.getUuid());
            if (connection == null) {
                continue;
            }
            ServerPlayer p = server.getPlayerList().getPlayer(senderState.getUuid());
            if (p == null) {
                continue;
            }
            if (!PluginManager.instance().onSoundPacket(sender, senderState, p, state, groupSoundPacket, SoundPacketEvent.SOURCE_GROUP)) {
                connection.send(this, soundMessage);
            }
        }
    }

    private void processProximityPacket(PlayerState senderState, ServerPlayer sender, MicPacket packet) throws Exception {
        double distance = Voicechat.SERVER_CONFIG.voiceChatDistance.get();
        @Nullable ClientGroup group = senderState.getGroup();

        SoundPacket<?> soundPacket = null;
        String source = null;
        if (sender.isSpectator()) {
            if (Voicechat.SERVER_CONFIG.spectatorPlayerPossession.get()) {
                Entity camera = sender.getCamera();
                if (camera instanceof ServerPlayer spectatingPlayer) {
                    if (spectatingPlayer != sender) {
                        PlayerState receiverState = playerStateManager.getState(spectatingPlayer.getUUID());
                        ClientConnection connection = connections.get(receiverState.getUuid());
                        if (connection == null) {
                            return;
                        }
                        GroupSoundPacket groupSoundPacket = new GroupSoundPacket(senderState.getUuid(), packet.getData(), packet.getSequenceNumber());
                        if (!PluginManager.instance().onSoundPacket(sender, senderState, spectatingPlayer, receiverState, groupSoundPacket, SoundPacketEvent.SOURCE_SPECTATOR)) {
                            connection.send(this, new NetworkMessage(groupSoundPacket));
                        }
                        return;
                    }
                }
            }
            if (Voicechat.SERVER_CONFIG.spectatorInteraction.get()) {
                soundPacket = new LocationSoundPacket(sender.getUUID(), sender.getEyePosition(), packet.getData(), packet.getSequenceNumber());
                source = SoundPacketEvent.SOURCE_SPECTATOR;
            }
        }

        if (soundPacket == null) {
            soundPacket = new PlayerSoundPacket(sender.getUUID(), packet.getData(), packet.getSequenceNumber(), packet.isWhispering());
            source = SoundPacketEvent.SOURCE_PROXIMITY;
        }

        broadcast(ServerWorldUtils.getPlayersInRange(sender.getLevel(), sender.position(), distance, p -> !p.getUUID().equals(sender.getUUID())), soundPacket, sender, senderState, group, source);
    }

    public void broadcast(Collection<ServerPlayer> players, SoundPacket<?> packet, @Nullable ServerPlayer sender, @Nullable PlayerState senderState, @Nullable ClientGroup group, String source) {
        for (ServerPlayer player : players) {
            PlayerState state = playerStateManager.getState(player.getUUID());
            if (state == null) {
                continue;
            }
            if (state.isDisabled() || state.isDisconnected()) {
                continue;
            }
            if (state.hasGroup() && state.getGroup().equals(group)) {
                continue;
            }
            ClientConnection connection = connections.get(player.getGameProfile().getId());
            if (connection == null) {
                continue;
            }
            try {
                if (!PluginManager.instance().onSoundPacket(sender, senderState, player, state, packet, source)) {
                    connection.send(this, new NetworkMessage(packet));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendKeepAlives() throws Exception {
        long timestamp = System.currentTimeMillis();

        connections.values().removeIf(connection -> {
            if (timestamp - connection.getLastKeepAliveResponse() >= Voicechat.SERVER_CONFIG.keepAlive.get() * 10L) {
                // Don't call disconnectClient here!
                secrets.remove(connection.getPlayerUUID());
                Voicechat.LOGGER.info("Player {} timed out", connection.getPlayerUUID());
                ServerPlayer player = server.getPlayerList().getPlayer(connection.getPlayerUUID());
                if (player != null) {
                    Voicechat.LOGGER.info("Reconnecting player {}", player.getDisplayName().getString());
                    Voicechat.SERVER.initializePlayerConnection(player);
                } else {
                    Voicechat.LOGGER.warn("Reconnecting player {} failed (Could not find player)", connection.getPlayerUUID());
                }
                PluginManager.instance().onPlayerDisconnected(server, connection.getPlayerUUID());
                return true;
            }
            return false;
        });

        for (ClientConnection connection : connections.values()) {
            sendPacket(new KeepAlivePacket(), connection);
        }

    }

    public Map<UUID, ClientConnection> getConnections() {
        return connections;
    }

    public VoicechatSocket getSocket() {
        return socket;
    }

    public int getPort() {
        return socket.getLocalPort();
    }

    public void sendPacket(Packet<?> packet, ClientConnection connection) throws Exception {
        connection.send(this, new NetworkMessage(packet));
    }

    public PingManager getPingManager() {
        return pingManager;
    }

    public PlayerStateManager getPlayerStateManager() {
        return playerStateManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public MinecraftServer getServer() {
        return server;
    }
}