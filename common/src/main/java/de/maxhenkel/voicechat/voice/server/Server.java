package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.RawUdpPacket;
import de.maxhenkel.voicechat.api.VoicechatSocket;
import de.maxhenkel.voicechat.api.events.SoundPacketEvent;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {

    private static final String NONCE_MAC = "HmacSHA256";
    private static final int NONCE_KEY_SIZE_BYTES = 32;

    private final Map<UUID, ClientConnection> connections;
    private final Map<UUID, UUID> connectionIds;
    private final Map<UUID, Secret> secrets;
    private final SecretKeySpec nonceKey;
    private final boolean dedicated;
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
        dedicated = server instanceof DedicatedServer;
        if (dedicated) {
            if (!server.usesAuthentication()) {
                Voicechat.LOGGER.warn("Running in offline mode - Voice chat encryption is not secure!");
            }
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
        connections = new ConcurrentHashMap<>();
        connectionIds = new ConcurrentHashMap<>();
        secrets = new ConcurrentHashMap<>();
        nonceKey = new SecretKeySpec(Utils.generateSecureRandomBytes(NONCE_KEY_SIZE_BYTES), NONCE_MAC);
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

    public void onPlayerLoggedIn(ServerPlayer player) {
        playerStateManager.onPlayerLoggedIn(player);
    }

    public void onPlayerLoggedOut(ServerPlayer player) {
        this.disconnectClient(player.getUUID());
        playerStateManager.onPlayerLoggedOut(player);
        groupManager.onPlayerLoggedOut(player);
    }

    public void onPlayerHide(ServerPlayer visibilityChangedPlayer, ServerPlayer observingPlayer) {
        playerStateManager.onPlayerHide(visibilityChangedPlayer, observingPlayer);
    }

    public void onPlayerShow(ServerPlayer visibilityChangedPlayer, ServerPlayer observingPlayer) {
        playerStateManager.onPlayerShow(visibilityChangedPlayer, observingPlayer);
    }

    public void onPlayerVoicechatConnect(ServerPlayer player) {
        playerStateManager.onPlayerVoicechatConnect(player);
    }

    public void onPlayerVoicechatDisconnect(UUID uuid) {
        playerStateManager.onPlayerVoicechatDisconnect(uuid);
    }

    public void onPlayerCompatibilityCheckSucceeded(ServerPlayer player) {
        playerStateManager.onPlayerCompatibilityCheckSucceeded(player);
        groupManager.onPlayerCompatibilityCheckSucceeded(player);
        categoryManager.onPlayerCompatibilityCheckSucceeded(player);
    }

    @Override
    public void run() {
        try {
            String bindAddress = getBindAddress();
            try {
                InetAddress.getByName(bindAddress);
            } catch (UnknownHostException e) {
                Voicechat.LOGGER.error("Failed to parse bind IP address '{}'", bindAddress, e);
                Voicechat.LOGGER.info("Binding to wildcard IP address");
                bindAddress = "";
            }
            socket.open(port, bindAddress);

            if (bindAddress.isEmpty()) {
                Voicechat.LOGGER.info("Voice chat server started at port {}", socket.getLocalPort());
            } else {
                Voicechat.LOGGER.info("Voice chat server started at {}:{}", bindAddress, socket.getLocalPort());
            }

            while (!socket.isClosed()) {
                try {
                    packetQueue.add(socket.read());
                } catch (ProtocolException e) {
                    if (Voicechat.debugMode()) {
                        Voicechat.LOGGER.warn("Failed to read packet", e);
                    } else {
                        CooldownTimer.run("socket_protocol_error", () -> {
                            Voicechat.LOGGER.warn("Failed to read packet: {}", e.getMessage());
                        });
                    }
                } catch (Exception e) {
                    // Only log an error if the error isn't caused by the socket being closed
                    if (!(e instanceof SocketException && e.getCause() instanceof AsynchronousCloseException)) {
                        if (Voicechat.debugMode()) {
                            Voicechat.LOGGER.error("Failed to read from socket", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Voicechat.LOGGER.error("Voice chat server error", e);
        }
    }

    private String getBindAddress() {
        if (!dedicated) {
            return "";
        }

        String bindAddress = Voicechat.SERVER_CONFIG.voiceChatBindAddress.get();

        if (bindAddress.trim().equals("*")) {
            bindAddress = "";
        } else if (bindAddress.trim().equals("")) {
            if (server instanceof DedicatedServer) {
                bindAddress = ((DedicatedServer) server).getProperties().serverIp;
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
        connectionIds.clear();
        secrets.clear();
    }

    public Secret getSecret(UUID playerUUID) {
        UUID connectionId = connectionIds.get(playerUUID);
        if (connectionId != null) {
            return secrets.get(connectionId);
        }
        connectionId = Utils.generateSecureRandomUUID();
        Secret secret = Secret.generateNewRandomSecret();
        secrets.put(connectionId, secret);
        connectionIds.put(playerUUID, connectionId);
        return secret;
    }

    /**
     * @param playerUUID the player uuid
     * @return the new secret or null if the player already has a secret
     */
    @Nullable
    public Secret generateNewSecret(UUID playerUUID) {
        if (hasSecret(playerUUID)) {
            return null;
        }
        return getSecret(playerUUID);
    }

    public boolean hasSecret(UUID playerUUID) {
        return connectionIds.containsKey(playerUUID);
    }

    /**
     * @param playerUUID the player uuid
     * @return the random ID that identifies the player in voice chat packets or null if the player has no secret
     */
    @Nullable
    public UUID getConnectionId(UUID playerUUID) {
        return connectionIds.get(playerUUID);
    }

    @Nullable
    public Secret getSecretByConnectionId(UUID connectionId) {
        return secrets.get(connectionId);
    }

    /**
     * Creates a nonce that only this server can create for the given connection and address.
     * The server sends it to the address in the {@link AuthenticateAckPacket} and only accepts a {@link ConnectionCheckPacket} with it from the same address.
     * This way, a client can only connect from an address it can receive packets at.
     *
     * @param connectionId the connection ID of the player
     * @param address      the address of the client
     * @return the nonce
     */
    private byte[] createNonce(UUID connectionId, SocketAddress address) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(NONCE_MAC);
        mac.init(nonceKey);
        mac.update(ByteBuffer.allocate(16).putLong(connectionId.getMostSignificantBits()).putLong(connectionId.getLeastSignificantBits()).array());
        mac.update(addressToBytes(address));
        return Arrays.copyOf(mac.doFinal(), Secret.NONCE_SIZE_BYTES);
    }

    private static byte[] addressToBytes(SocketAddress address) {
        if (!(address instanceof InetSocketAddress)) {
            return address.toString().getBytes(StandardCharsets.UTF_8);
        }
        InetSocketAddress inetAddress = (InetSocketAddress) address;
        byte[] ip = inetAddress.getAddress().getAddress();
        return ByteBuffer.allocate(ip.length + 4).put(ip).putInt(inetAddress.getPort()).array();
    }

    public void disconnectClient(UUID playerUUID) {
        connections.remove(playerUUID);
        removeSecret(playerUUID);
        PluginManager.instance().onPlayerDisconnected(playerUUID);
    }

    private void removeSecret(UUID playerUUID) {
        UUID connectionId = connectionIds.remove(playerUUID);
        if (connectionId != null) {
            secrets.remove(connectionId);
        }
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

                    if (message.getPacket() instanceof AuthenticatePacket packet) {
                        UUID connectionId = connectionIds.get(packet.getPlayerUUID());
                        if (connectionId != null && packet.getSecret().equals(secrets.get(connectionId))) {
                            // The connection is only created once the client sends the nonce back from the same address
                            sendPacket(new AuthenticateAckPacket(createNonce(connectionId, message.getAddress())), new ClientConnection(packet.getPlayerUUID(), message.getAddress()));
                            Voicechat.LOGGER.info("Successfully authenticated player {}", packet.getPlayerUUID());
                        }
                    }

                    if (message.getPacket() instanceof ConnectionCheckPacket packet) {
                        UUID connectionId = connectionIds.get(packet.getPlayerUUID());
                        if (connectionId == null || !MessageDigest.isEqual(createNonce(connectionId, message.getAddress()), packet.getNonce())) {
                            continue;
                        }
                        ClientConnection connection = connections.get(packet.getPlayerUUID());
                        if (connection != null) {
                            if (connection.getAddress().equals(message.getAddress())) {
                                sendPacket(new ConnectionCheckAckPacket(), connection);
                            }
                            continue;
                        }
                        connection = new ClientConnection(packet.getPlayerUUID(), message.getAddress());
                        connections.put(connection.getPlayerUUID(), connection);
                        Voicechat.LOGGER.info("Successfully validated connection of player {}", connection.getPlayerUUID());
                        ServerPlayer player = server.getPlayerList().getPlayer(connection.getPlayerUUID());
                        if (player != null) {
                            CommonCompatibilityManager.INSTANCE.emitServerVoiceChatConnectedEvent(player);
                            PluginManager.instance().onPlayerConnected(player);
                            playerStateManager.updateForcedState(player);
                            Voicechat.LOGGER.info("Player {} ({}) successfully connected to voice chat", player.getName().getString(), connection.getPlayerUUID());
                        }
                        sendPacket(new ConnectionCheckAckPacket(), connection);
                        continue;
                    }

                    ClientConnection conn = getSender(message);
                    if (conn == null) {
                        continue;
                    }

                    if (message.getPacket() instanceof MicPacket packet) {
                        onMicPacket(conn.getPlayerUUID(), packet);
                    } else if (message.getPacket() instanceof PingPacket packet) {
                        pingManager.onPongPacket(packet);
                    } else if (message.getPacket() instanceof KeepAlivePacket) {
                        conn.setLastKeepAliveResponse(System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    Voicechat.LOGGER.error("Voice chat server error", e);
                }
            }
        }

        public void close() {
            running = false;
        }
    }

    public void onMicPacket(UUID playerUuid, MicPacket packet) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
        if (player == null) {
            return;
        }
        PlayerState state = playerStateManager.getState(player.getUUID());
        if (state == null || state.isForceMuted()) {
            return;
        }
        if (!PluginManager.instance().onMicPacket(player, state, packet)) {
            processMicPacket(player, state, packet);
        }
    }

    private void processMicPacket(ServerPlayer player, PlayerState state, MicPacket packet) {
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

    private void processGroupPacket(PlayerState senderState, ServerPlayer sender, MicPacket packet) {
        UUID groupId = senderState.getGroup();
        if (groupId == null) {
            return;
        }
        GroupSoundPacket groupSoundPacket = new GroupSoundPacket(senderState.getUuid(), senderState.getUuid(), packet.getData(), packet.getSequenceNumber(), null);
        for (PlayerState state : playerStateManager.getStates()) {
            if (!groupId.equals(state.getGroup())) {
                continue;
            }
            if (senderState.getUuid().equals(state.getUuid())) {
                continue;
            }
            ServerPlayer p = server.getPlayerList().getPlayer(state.getUuid());
            if (p == null) {
                continue;
            }
            @Nullable ClientConnection connection = getConnection(state.getUuid());
            sendSoundPacket(sender, senderState, p, state, connection, groupSoundPacket, SoundPacketEvent.SOURCE_GROUP);
        }
    }

    private void processProximityPacket(PlayerState senderState, ServerPlayer sender, MicPacket packet) {
        @Nullable UUID groupId = senderState.getGroup();
        float distance;
        if (packet.isWhispering()) {
            distance = Voicechat.SERVER_CONFIG.whisperDistance.get().floatValue();
        } else {
            distance = Utils.getDefaultDistanceServer();
        }

        distance = PluginManager.instance().getDistance(sender, senderState, packet, distance);

        SoundPacket<?> soundPacket = null;
        String source = null;
        if (sender.isSpectator()) {
            if (Voicechat.SERVER_CONFIG.spectatorPlayerPossession.get()) {
                Entity camera = sender.getCamera();
                if (camera instanceof ServerPlayer spectatingPlayer) {
                    if (spectatingPlayer != sender) {
                        PlayerState receiverState = playerStateManager.getState(spectatingPlayer.getUUID());
                        if (receiverState == null) {
                            return;
                        }
                        GroupSoundPacket groupSoundPacket = new GroupSoundPacket(senderState.getUuid(), senderState.getUuid(), packet.getData(), packet.getSequenceNumber(), null);
                        @Nullable ClientConnection connection = getConnection(receiverState.getUuid());
                        sendSoundPacket(sender, senderState, spectatingPlayer, receiverState, connection, groupSoundPacket, SoundPacketEvent.SOURCE_SPECTATOR);
                        return;
                    }
                }
            }
            if (Voicechat.SERVER_CONFIG.spectatorInteraction.get()) {
                soundPacket = new LocationSoundPacket(sender.getUUID(), sender.getUUID(), sender.getEyePosition(), packet.getData(), packet.getSequenceNumber(), distance, null);
                source = SoundPacketEvent.SOURCE_SPECTATOR;
            }
        }

        if (soundPacket == null) {
            soundPacket = new PlayerSoundPacket(sender.getUUID(), sender.getUUID(), packet.getData(), packet.getSequenceNumber(), packet.isWhispering(), distance, null);
            source = SoundPacketEvent.SOURCE_PROXIMITY;
        }

        broadcast(ServerPlayerManager.getPlayersInRange(sender.serverLevel(), sender.position(), getBroadcastRange(distance), p -> !p.getUUID().equals(sender.getUUID())), soundPacket, sender, senderState, groupId, source);
    }

    public void sendSoundPacket(@Nullable ServerPlayer sender, @Nullable PlayerState senderState, ServerPlayer receiver, PlayerState receiverState, @Nullable ClientConnection connection, SoundPacket<?> soundPacket, String source) {
        if (soundPacket.getData().length > AudioUtils.MAX_OPUS_PAYLOAD_SIZE) {
            CooldownTimer.run("packet_data_too_large", () -> {
                Voicechat.LOGGER.warn("Tried to send a packet that's too large ({} bytes > {} bytes)", soundPacket.getData().length, AudioUtils.MAX_OPUS_PAYLOAD_SIZE);
            });
            return;
        }
        PluginManager.instance().onListenerAudio(receiver.getUUID(), soundPacket);

        if (connection == null) {
            return;
        }

        if (receiverState.isDisabled() || receiverState.isForceDisabled() || receiverState.isDisconnected()) {
            return;
        }

        if (PluginManager.instance().onSoundPacket(sender, senderState, receiver, receiverState, soundPacket, source)) {
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

    public void broadcast(Collection<ServerPlayer> players, SoundPacket<?> packet, @Nullable ServerPlayer sender, @Nullable PlayerState senderState, @Nullable UUID groupId, String source) {
        for (ServerPlayer player : players) {
            PlayerState state = playerStateManager.getState(player.getUUID());
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
            sendSoundPacket(sender, senderState, player, state, connection, packet, source);
        }
    }

    private void sendKeepAlives() {
        long timestamp = System.currentTimeMillis();

        connections.values().removeIf(connection -> {
            if (timestamp - connection.getLastKeepAliveResponse() >= Voicechat.SERVER_CONFIG.keepAlive.get() * 10L) {
                // Don't call disconnectClient here!
                removeSecret(connection.getPlayerUUID());
                Voicechat.LOGGER.info("Player {} timed out", connection.getPlayerUUID());
                ServerPlayer player = server.getPlayerList().getPlayer(connection.getPlayerUUID());
                if (player != null) {
                    Voicechat.LOGGER.info("Reconnecting player {}", player.getName().getString());
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
            ServerPlayer player = server.getPlayerList().getPlayer(connection.getPlayerUUID());
            if (player != null) {
                playerStateManager.updateForcedState(player);
            }
        }

    }

    @Nullable
    public ClientConnection getSender(NetworkMessage message) {
        // The address alone can be spoofed, so the message also has to be encrypted with the secret of the connection
        return connections
                .values()
                .stream()
                .filter(connection -> connection.getAddress().equals(message.getAddress()))
                .filter(connection -> message.getConnectionId().equals(connectionIds.get(connection.getPlayerUUID())))
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

    /**
     * Sends the packet and handles potential errors.
     *
     * @param packet     the packet to send
     * @param connection the connection to send the packet to
     * @return if the packet was sent successfully
     */
    public boolean sendPacket(Packet<?> packet, ClientConnection connection) {
        try {
            sendPacketRaw(packet, connection);
            return true;
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to send voice chat packet to {}", connection.getPlayerUUID());
            return false;
        }
    }

    /**
     * Sends the packet. You must handle potential errors
     *
     * @param packet     the packet to send
     * @param connection the connection to send the packet to
     * @throws Exception if an I/O error occurs
     */
    public void sendPacketRaw(Packet<?> packet, ClientConnection connection) throws Exception {
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