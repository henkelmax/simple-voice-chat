package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.api.VoicechatSocket;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {

    private final Map<UUID, ClientConnection> connections;
    private final Map<UUID, ClientConnection> unCheckedConnections;
    private final Map<UUID, UUID> secrets;
    private final int port;
    private final org.bukkit.Server server;
    private final VoicechatSocket socket;
    private final ProcessThread processThread;
    private final BlockingQueue<RawUdpPacket> packetQueue;
    private final PingManager pingManager;
    private final PlayerStateManager playerStateManager;
    private final ServerGroupManager groupManager;
    private final ServerCategoryManager categoryManager;

    public Server() {
        int configPort = Voicechat.SERVER_CONFIG.voiceChatPort.get();
        if (configPort < 0) {
            Voicechat.LOGGER.info("Using the Minecraft servers port as voice chat port");
            port = Bukkit.getPort();
        } else {
            port = configPort;
        }
        this.server = Bukkit.getServer();
        socket = PluginManager.instance().getSocketImplementation();
        connections = new HashMap<>();
        unCheckedConnections = new HashMap<>();
        secrets = new HashMap<>();
        packetQueue = new LinkedBlockingQueue<>();
        pingManager = new PingManager(this);
        playerStateManager = new PlayerStateManager();
        groupManager = new ServerGroupManager();
        categoryManager = new ServerCategoryManager();
        setDaemon(true);
        setName("VoiceChatServerThread");
        processThread = new ProcessThread();
        processThread.start();
    }

    @Override
    public void run() {
        try {
            String bindAddress = Voicechat.SERVER_CONFIG.voiceChatBindAddress.get();

            if (bindAddress.trim().equals("*")) {
                bindAddress = "";
            } else if (bindAddress.trim().isEmpty()) {
                try {
                    bindAddress = Voicechat.compatibility.getServerIp(server);
                    if (!bindAddress.trim().isEmpty()) {
                        Voicechat.LOGGER.info("Using server-ip as bind address: {}", bindAddress);
                    }
                } catch (Throwable t) {
                    Voicechat.LOGGER.warn("Failed to get server-ip from server.properties - binding to wildcard address", t);
                }
            }

            socket.open(port, bindAddress);
            Voicechat.LOGGER.info("Server started at port {}", socket.getLocalPort());

            while (!socket.isClosed()) {
                try {
                    packetQueue.add(socket.read());
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            Voicechat.LOGGER.error("Voice chat server error {}", e.getMessage());
        }
    }

    public UUID getSecret(UUID playerUUID) {
        if (hasSecret(playerUUID)) {
            return secrets.get(playerUUID);
        } else {
            SecureRandom r = new SecureRandom();
            UUID secret = new UUID(r.nextLong(), r.nextLong());
            secrets.put(playerUUID, secret);
            return secret;
        }
    }

    public boolean hasSecret(UUID playerUUID) {
        return secrets.containsKey(playerUUID);
    }

    public void disconnectClient(UUID playerUUID) {
        connections.remove(playerUUID);
        unCheckedConnections.remove(playerUUID);
        secrets.remove(playerUUID);
        PluginManager.instance().onPlayerDisconnected(playerUUID);
    }

    public void close() {
        socket.close();
        processThread.close();

        PluginManager.instance().onServerStopped();
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
                    } catch (IndexOutOfBoundsException | BadPaddingException | NoSuchPaddingException |
                             IllegalBlockSizeException | InvalidKeyException e) {
                        CooldownTimer.run("failed_reading_packet", () -> {
                            Voicechat.LOGGER.warn("Failed to read packet from {}", rawPacket.getSocketAddress());
                        });
                        continue;
                    }

                    if (message == null) {
                        continue;
                    }

                    if (System.currentTimeMillis() - message.getTimestamp() > message.getTTL()) {
                        CooldownTimer.run("ttl", () -> {
                            Voicechat.LOGGER.error("Dropping voice chat packets! Your Server might be overloaded!");
                            Voicechat.LOGGER.error("Packet queue has {} packets", packetQueue.size());
                        });
                        continue;
                    }

                    if (message.getPacket() instanceof AuthenticatePacket) {
                        AuthenticatePacket packet = (AuthenticatePacket) message.getPacket();
                        UUID secret = secrets.get(packet.getPlayerUUID());
                        if (secret != null && secret.equals(packet.getSecret())) {
                            ClientConnection connection = unCheckedConnections.get(packet.getPlayerUUID());
                            if (connection == null) {
                                connection = connections.get(packet.getPlayerUUID());
                            }
                            if (connection == null) {
                                connection = new ClientConnection(packet.getPlayerUUID(), message.getAddress());
                                unCheckedConnections.put(packet.getPlayerUUID(), connection);
                                Voicechat.LOGGER.info("Successfully authenticated player {}", packet.getPlayerUUID());
                            }
                            sendPacket(new AuthenticateAckPacket(), connection);
                        }
                    }

                    if (message.getPacket() instanceof ConnectionCheckPacket) {
                        ClientConnection connection = getUnconnectedSender(message);
                        if (connection == null) {
                            connection = getSender(message);
                            if (connection != null) {
                                sendPacket(new ConnectionCheckAckPacket(), connection);
                            }
                            continue;
                        }
                        // Refresh keepalive, so players who took longer than the timeout can still connect
                        connection.setLastKeepAliveResponse(System.currentTimeMillis());
                        connections.put(connection.getPlayerUUID(), connection);
                        unCheckedConnections.remove(connection.getPlayerUUID());
                        Voicechat.LOGGER.info("Successfully validated connection of player {}", connection.getPlayerUUID());
                        Player player = server.getPlayer(connection.getPlayerUUID());
                        if (player != null) {
                            playerStateManager.onPlayerVoicechatConnect(player);
                            PluginManager.instance().onPlayerConnected(player);
                            Voicechat.LOGGER.info("Player {} ({}) successfully connected to voice chat", player.getName(), connection.getPlayerUUID());
                        }
                        sendPacket(new ConnectionCheckAckPacket(), connection);
                        continue;
                    }

                    ClientConnection conn = getSender(message);
                    if (conn == null) {
                        continue;
                    }

                    UUID playerUUID = conn.getPlayerUUID();

                    if (message.getPacket() instanceof MicPacket) {
                        MicPacket packet = (MicPacket) message.getPacket();
                        Player player = server.getPlayer(playerUUID);
                        if (player == null) {
                            continue;
                        }
                        if (!player.hasPermission(PermissionManager.SPEAK_PERMISSION)) {
                            CooldownTimer.run("no-speak-" + playerUUID, () -> {
                                NetManager.sendStatusMessage(player, Component.translatable("message.voicechat.no_speak_permission"));
                            });
                            continue;
                        }
                        PlayerState state = playerStateManager.getState(player.getUniqueId());
                        if (state == null) {
                            continue;
                        }
                        if (!PluginManager.instance().onMicPacket(player, state, packet)) {
                            processMicPacket(player, state, packet);
                        }
                    } else if (message.getPacket() instanceof PingPacket) {
                        PingPacket packet = (PingPacket) message.getPacket();
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

    private void processMicPacket(Player player, PlayerState state, MicPacket packet) throws Exception {
        if (state.hasGroup()) {
            @Nullable Group group = groupManager.getGroup(state.getGroup());
            processGroupPacket(state, player, packet);
            if (group == null || group.isOpen()) {
                processProximityPacket(state, player, packet);
            }
            return;
        }
        processProximityPacket(state, player, packet);
    }

    private void processGroupPacket(PlayerState senderState, Player sender, MicPacket packet) throws Exception {
        UUID groupId = senderState.getGroup();
        if (groupId == null) {
            return;
        }
        GroupSoundPacket groupSoundPacket = new GroupSoundPacket(senderState.getUuid(), packet.getData(), packet.getSequenceNumber(), null);
        for (PlayerState state : playerStateManager.getStates()) {
            if (!groupId.equals(state.getGroup())) {
                continue;
            }
            if (senderState.getUuid().equals(state.getUuid())) {
                continue;
            }
            Player p = server.getPlayer(state.getUuid());
            if (p == null) {
                continue;
            }
            @Nullable ClientConnection connection = getConnection(state.getUuid());
            sendSoundPacket(sender, senderState, p, state, connection, groupSoundPacket, SoundPacketEvent.SOURCE_GROUP);
        }
    }

    private void processProximityPacket(PlayerState senderState, Player sender, MicPacket packet) throws Exception {
        @Nullable UUID groupId = senderState.getGroup();
        float distance = Utils.getDefaultDistance();

        SoundPacket<?> soundPacket = null;
        String source = null;
        if (sender.getGameMode().equals(GameMode.SPECTATOR)) {
            if (Voicechat.SERVER_CONFIG.spectatorPlayerPossession.get()) {
                Entity camera = sender.getSpectatorTarget();
                if (camera instanceof Player) {
                    Player spectatingPlayer = (Player) camera;
                    if (spectatingPlayer != sender) {
                        PlayerState receiverState = playerStateManager.getState(spectatingPlayer.getUniqueId());
                        if (receiverState == null) {
                            return;
                        }
                        GroupSoundPacket groupSoundPacket = new GroupSoundPacket(senderState.getUuid(), packet.getData(), packet.getSequenceNumber(), null);
                        @Nullable ClientConnection connection = getConnection(receiverState.getUuid());
                        sendSoundPacket(sender, senderState, spectatingPlayer, receiverState, connection, groupSoundPacket, SoundPacketEvent.SOURCE_SPECTATOR);
                        return;
                    }
                }
            }
            if (Voicechat.SERVER_CONFIG.spectatorInteraction.get()) {
                soundPacket = new LocationSoundPacket(sender.getUniqueId(), sender.getLocation(), packet.getData(), packet.getSequenceNumber(), distance, null);
                source = SoundPacketEvent.SOURCE_SPECTATOR;
            }
        }

        if (soundPacket == null) {
            float crouchMultiplayer = sender.isSneaking() ? Voicechat.SERVER_CONFIG.crouchDistanceMultiplier.get().floatValue() : 1F;
            float whisperMultiplayer = packet.isWhispering() ? Voicechat.SERVER_CONFIG.whisperDistanceMultiplier.get().floatValue() : 1F;
            float multiplier = crouchMultiplayer * whisperMultiplayer;
            distance = distance * multiplier;
            soundPacket = new PlayerSoundPacket(sender.getUniqueId(), packet.getData(), packet.getSequenceNumber(), packet.isWhispering(), distance, null);
            source = SoundPacketEvent.SOURCE_PROXIMITY;
        }

        broadcast(ServerWorldUtils.getPlayersInRange(sender.getWorld(), sender.getLocation(), getBroadcastRange(distance), p -> !p.getUniqueId().equals(sender.getUniqueId())), soundPacket, sender, senderState, groupId, source);
    }

    public void sendSoundPacket(Player player, ClientConnection connection, SoundPacket<?> soundPacket) throws Exception {
        if (!player.hasPermission(PermissionManager.LISTEN_PERMISSION)) {
            CooldownTimer.run("no-listen-" + player.getUniqueId(), 30_000L, () -> {
                NetManager.sendStatusMessage(player, Component.translatable("message.voicechat.no_listen_permission"));
            });
            return;
        }
        connection.send(this, new NetworkMessage(soundPacket));
    }

    public void sendSoundPacket(@Nullable Player sender, @Nullable PlayerState senderState, Player receiver, PlayerState receiverState, @Nullable ClientConnection connection, SoundPacket<?> soundPacket, String source) throws Exception {
        PluginManager.instance().onListenerAudio(receiver.getUniqueId(), soundPacket);

        if (connection == null) {
            return;
        }

        if (PluginManager.instance().onSoundPacket(sender, senderState, receiver, receiverState, soundPacket, source)) {
            return;
        }

        if (!receiver.hasPermission(PermissionManager.LISTEN_PERMISSION)) {
            CooldownTimer.run(String.format("no-listen-%s", receiver.getUniqueId()), 30_000L, () -> {
                NetManager.sendStatusMessage(receiver, Component.translatable("message.voicechat.no_listen_permission"));
            });
            return;
        }
        sendPacket(soundPacket, connection);
    }

    public double getBroadcastRange(float minRange) {
        double broadcastRange = Voicechat.SERVER_CONFIG.broadcastRange.get();
        if (broadcastRange < 0D) {
            broadcastRange = Voicechat.SERVER_CONFIG.voiceChatDistance.get() + 1D;
        }
        return Math.max(broadcastRange, minRange);
    }

    public void broadcast(Collection<Player> players, SoundPacket<?> packet, @Nullable Player sender, @Nullable PlayerState senderState, @Nullable UUID groupId, String source) {
        for (Player player : players) {
            PlayerState state = playerStateManager.getState(player.getUniqueId());
            if (state == null) {
                continue;
            }
            if (state.isDisabled() || state.isDisconnected()) {
                continue;
            }
            if (state.hasGroup() && state.getGroup().equals(groupId)) {
                continue;
            }
            @Nullable Group receiverGroup = null;
            if (state.hasGroup()) {
                receiverGroup = groupManager.getGroup(state.getGroup());
            }
            if (receiverGroup != null && receiverGroup.isIsolated()) {
                continue;
            }
            @Nullable ClientConnection connection = getConnection(state.getUuid());
            try {
                sendSoundPacket(sender, senderState, player, state, connection, packet, source);
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
                Player player = server.getPlayer(connection.getPlayerUUID());
                if (player != null) {
                    Voicechat.LOGGER.info("Reconnecting player {}", player.getName());
                    Voicechat.SERVER.initializePlayerConnection(player);
                } else {
                    Voicechat.LOGGER.error("Reconnecting player {} failed (Could not find player)", connection.getPlayerUUID());
                }
                playerStateManager.onPlayerVoicechatDisconnect(connection.getPlayerUUID());
                PluginManager.instance().onPlayerDisconnected(connection.getPlayerUUID());
                return true;
            }
            return false;
        });

        for (ClientConnection connection : connections.values()) {
            sendPacket(new KeepAlivePacket(), connection);
        }

    }

    @Nullable
    public ClientConnection getSender(NetworkMessage message) {
        return connections
                .values()
                .stream()
                .filter(connection -> connection.getAddress().equals(message.getAddress()))
                .findAny()
                .orElse(null);
    }

    @Nullable
    public ClientConnection getUnconnectedSender(NetworkMessage message) {
        return unCheckedConnections
                .values()
                .stream()
                .filter(connection -> connection.getAddress().equals(message.getAddress()))
                .findAny()
                .orElse(null);
    }

    public Map<UUID, ClientConnection> getConnections() {
        return connections;
    }

    @Nullable
    public ClientConnection getConnection(UUID playerID) {
        return connections.get(playerID);
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

    public ServerGroupManager getGroupManager() {
        return groupManager;
    }

    public ServerCategoryManager getCategoryManager() {
        return categoryManager;
    }

}