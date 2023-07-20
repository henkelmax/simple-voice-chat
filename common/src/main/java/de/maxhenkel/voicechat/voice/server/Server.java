package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.api.VoicechatSocket;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.net.InetAddress;
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
    private int port;
    private final MinecraftServer server;
    private VoicechatSocket socket;
    private final ProcessThread processThread;
    private final BlockingQueue<RawUdpPacket> packetQueue;
    private final PingManager pingManager;
    private final PlayerStateManager playerStateManager;
    private final ServerGroupManager groupManager;
    private final ServerCategoryManager categoryManager;

    public Server(MinecraftServer server) {
        if (server.isDedicatedServer()) {
            int configPort = Voicechat.SERVER_CONFIG.voiceChatPort.get();
            if (configPort < 0) {
                Voicechat.LOGGER.info("Using the Minecraft servers port as voice chat port");
                port = ((DedicatedServer) server).getPort();
            } else {
                port = configPort;
            }
        } else {
            port = 0;
        }
        this.server = server;
        socket = PluginManager.instance().getSocketImplementation(server);
        connections = new HashMap<>();
        unCheckedConnections = new HashMap<>();
        secrets = new HashMap<>();
        packetQueue = new LinkedBlockingQueue<>();
        pingManager = new PingManager(this);
        playerStateManager = new PlayerStateManager(this);
        groupManager = new ServerGroupManager(this);
        categoryManager = new ServerCategoryManager(this);
        setDaemon(true);
        setName("VoiceChatServerThread");
        setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());
        processThread = new ProcessThread();
        processThread.start();
    }

    @Override
    public void run() {
        try {
            String bindAddress = getBindAddress();
            socket.open(port, bindAddress);

            if (bindAddress.isEmpty()) {
                Voicechat.LOGGER.info("Voice chat server started at port {}", socket.getLocalPort());
            } else {
                Voicechat.LOGGER.info("Voice chat server started at {}:{}", bindAddress, socket.getLocalPort());
            }

            while (!socket.isClosed()) {
                try {
                    packetQueue.add(socket.read());
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            Voicechat.LOGGER.error("Voice chat server error", e);
        }
    }

    private String getBindAddress() {
        String bindAddress = Voicechat.SERVER_CONFIG.voiceChatBindAddress.get();

        if (bindAddress.trim().equals("*")) {
            bindAddress = "";
        } else if (bindAddress.trim().isEmpty()) {
            if (server.isDedicatedServer() && server instanceof DedicatedServer) {
                bindAddress = ((DedicatedServer) server).getStringProperty("server-ip", "");
                if (!bindAddress.trim().isEmpty()) {
                    try {
                        InetAddress address = InetAddress.getByName(bindAddress);
                        if (address.isLoopbackAddress()) {
                            bindAddress = "";
                        } else {
                            Voicechat.LOGGER.info("Using server-ip as bind address: {}", bindAddress);
                        }
                    } catch (Exception e) {
                        Voicechat.LOGGER.warn("Invalid server-ip", e);
                        bindAddress = "";
                    }
                }
            }
        }
        return bindAddress;
    }

    /**
     * Changes the port of the voice chat server.
     * <b>NOTE:</b> This removes every existing connection and all secrets!
     *
     * @param port the new voice chat port
     * @throws Exception if an error opening the socket on the new port occurs
     */
    public void changePort(int port) throws Exception {
        VoicechatSocket newSocket = PluginManager.instance().getSocketImplementation(server);
        newSocket.open(port, getBindAddress());
        VoicechatSocket old = socket;
        socket = newSocket;
        this.port = port;
        old.close();
        connections.clear();
        unCheckedConnections.clear();
        secrets.clear();
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
            setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());
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

                    if (message == null) {
                        continue;
                    }

                    if (System.currentTimeMillis() - message.getTimestamp() > message.getTTL()) {
                        CooldownTimer.run("ttl", () -> {
                            Voicechat.LOGGER.warn("Dropping voice chat packets! Your Server might be overloaded!");
                            Voicechat.LOGGER.warn("Packet queue has {} packets", packetQueue.size());
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
                        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(connection.getPlayerUUID());
                        if (player != null) {
                            CommonCompatibilityManager.INSTANCE.emitServerVoiceChatConnectedEvent(player);
                            PluginManager.instance().onPlayerConnected(player);
                            Voicechat.LOGGER.info("Player {} ({}) successfully connected to voice chat", player.getDisplayNameString(), connection.getPlayerUUID());
                        }
                        sendPacket(new ConnectionCheckAckPacket(), connection);
                        continue;
                    }

                    ClientConnection conn = getSender(message);
                    if (conn == null) {
                        continue;
                    }

                    if (message.getPacket() instanceof MicPacket) {
                        MicPacket packet = (MicPacket) message.getPacket();
                        onMicPacket(conn.getPlayerUUID(), packet);
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

    public void onMicPacket(UUID playerUuid, MicPacket packet) throws Exception {
        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerUuid);
        if (player == null) {
            return;
        }
        if (!PermissionManager.INSTANCE.SPEAK_PERMISSION.hasPermission(player)) {
            CooldownTimer.run("no-speak-" + playerUuid, 30_000L, () -> {
                player.sendStatusMessage(new TextComponentTranslation("message.voicechat.no_speak_permission"), true);
            });
            return;
        }
        PlayerState state = playerStateManager.getState(player.getUniqueID());
        if (state == null) {
            return;
        }
        if (!PluginManager.instance().onMicPacket(player, state, packet)) {
            processMicPacket(player, state, packet);
        }
    }

    private void processMicPacket(EntityPlayerMP player, PlayerState state, MicPacket packet) throws Exception {
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

    private void processGroupPacket(PlayerState senderState, EntityPlayerMP sender, MicPacket packet) throws Exception {
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
            EntityPlayerMP p = server.getPlayerList().getPlayerByUUID(state.getUuid());
            if (p == null) {
                continue;
            }
            @Nullable ClientConnection connection = getConnection(state.getUuid());
            sendSoundPacket(sender, senderState, p, state, connection, groupSoundPacket, SoundPacketEvent.SOURCE_GROUP);
        }
    }

    private void processProximityPacket(PlayerState senderState, EntityPlayerMP sender, MicPacket packet) throws Exception {
        @Nullable UUID groupId = senderState.getGroup();
        float distance = Utils.getDefaultDistance();

        SoundPacket<?> soundPacket = null;
        String source = null;
        if (sender.isSpectator()) {
            if (Voicechat.SERVER_CONFIG.spectatorPlayerPossession.get()) {
                Entity camera = sender.getSpectatingEntity();
                if (camera instanceof EntityPlayerMP) {
                    EntityPlayerMP spectatingPlayer = (EntityPlayerMP) camera;
                    if (spectatingPlayer != sender) {
                        PlayerState receiverState = playerStateManager.getState(spectatingPlayer.getUniqueID());
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
                soundPacket = new LocationSoundPacket(sender.getUniqueID(), sender.getPositionEyes(1F), packet.getData(), packet.getSequenceNumber(), distance, null);
                source = SoundPacketEvent.SOURCE_SPECTATOR;
            }
        }

        if (soundPacket == null) {
            float crouchMultiplayer = sender.isSneaking() ? Voicechat.SERVER_CONFIG.crouchDistanceMultiplier.get().floatValue() : 1F;
            float whisperMultiplayer = packet.isWhispering() ? Voicechat.SERVER_CONFIG.whisperDistanceMultiplier.get().floatValue() : 1F;
            float multiplier = crouchMultiplayer * whisperMultiplayer;
            distance = distance * multiplier;
            soundPacket = new PlayerSoundPacket(sender.getUniqueID(), packet.getData(), packet.getSequenceNumber(), packet.isWhispering(), distance, null);
            source = SoundPacketEvent.SOURCE_PROXIMITY;
        }

        broadcast(ServerWorldUtils.getPlayersInRange(sender.getServerWorld(), sender.getPositionVector(), getBroadcastRange(distance), p -> !p.getUniqueID().equals(sender.getUniqueID())), soundPacket, sender, senderState, groupId, source);
    }

    public void sendSoundPacket(@Nullable EntityPlayerMP sender, @Nullable PlayerState senderState, EntityPlayerMP receiver, PlayerState receiverState, @Nullable ClientConnection connection, SoundPacket<?> soundPacket, String source) throws Exception {
        PluginManager.instance().onListenerAudio(receiver.getUniqueID(), soundPacket);

        if (connection == null) {
            return;
        }

        if (receiverState.isDisabled() || receiverState.isDisconnected()) {
            return;
        }

        if (PluginManager.instance().onSoundPacket(sender, senderState, receiver, receiverState, soundPacket, source)) {
            return;
        }

        if (!PermissionManager.INSTANCE.LISTEN_PERMISSION.hasPermission(receiver)) {
            CooldownTimer.run(String.format("no-listen-%s", receiver.getUniqueID()), 30_000L, () -> {
                receiver.sendStatusMessage(new TextComponentTranslation("message.voicechat.no_listen_permission"), true);
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

    public void broadcast(Collection<EntityPlayerMP> players, SoundPacket<?> packet, @Nullable EntityPlayerMP sender, @Nullable PlayerState senderState, @Nullable UUID groupId, String source) {
        for (EntityPlayerMP player : players) {
            PlayerState state = playerStateManager.getState(player.getUniqueID());
            if (state == null) {
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
                EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(connection.getPlayerUUID());
                if (player != null) {
                    Voicechat.LOGGER.info("Reconnecting player {}", player.getDisplayNameString());
                    Voicechat.SERVER.initializePlayerConnection(player);
                } else {
                    Voicechat.LOGGER.warn("Reconnecting player {} failed (Could not find player)", connection.getPlayerUUID());
                }
                CommonCompatibilityManager.INSTANCE.emitServerVoiceChatDisconnectedEvent(connection.getPlayerUUID());
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

    public MinecraftServer getServer() {
        return server;
    }
}