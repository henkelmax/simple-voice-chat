package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.command.VoiceChatCommands;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.voice.common.*;
import org.bukkit.entity.Player;

import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {

    private Map<UUID, ClientConnection> connections;
    private Map<UUID, UUID> secrets;
    private int port;
    private org.bukkit.Server server;
    private DatagramSocket socket;
    private ProcessThread processThread;
    private BlockingQueue<NetworkMessage.UnprocessedNetworkMessage> packetQueue;
    private PingManager pingManager;
    private PlayerStateManager playerStateManager;

    public Server(int port, org.bukkit.Server server) {
        this.port = port;
        this.server = server;
        connections = new HashMap<>();
        secrets = new HashMap<>();
        packetQueue = new LinkedBlockingQueue<>();
        pingManager = new PingManager(this);
        playerStateManager = new PlayerStateManager();
        setDaemon(true);
        setName("VoiceChatServerThread");
        processThread = new ProcessThread();
        processThread.start();
    }

    @Override
    public void run() {
        try {
            InetAddress address = null;
            String addr = Voicechat.SERVER_CONFIG.voiceChatBindAddress.get();
            try {
                if (!addr.isEmpty()) {
                    address = InetAddress.getByName(addr);
                }
            } catch (Exception e) {
                Voicechat.LOGGER.error("Failed to parse bind IP address '" + addr + "'");
                Voicechat.LOGGER.info("Binding to default IP address");
                e.printStackTrace();
            }
            try {
                try {
                    socket = new DatagramSocket(port, address);
                } catch (BindException e) {
                    if (address == null || addr.equals("0.0.0.0")) {
                        throw e;
                    }
                    Voicechat.LOGGER.fatal("Failed to bind to address '" + addr + "', binding to '0.0.0.0' instead");
                    socket = new DatagramSocket(port);
                }
                socket.setTrafficClass(0x04); // IPTOS_RELIABILITY
            } catch (BindException e) {
                Voicechat.LOGGER.error("Failed to bind to address '" + addr + "'");
                e.printStackTrace();
                System.exit(1);
                return;
            }
            Voicechat.LOGGER.info("Server started at port " + port);

            while (!socket.isClosed()) {
                try {
                    packetQueue.add(NetworkMessage.readPacket(socket));
                } catch (Exception e) {
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public UUID getSecret(UUID playerUUID) {
        if (secrets.containsKey(playerUUID)) {
            return secrets.get(playerUUID);
        } else {
            UUID secret = UUID.randomUUID();
            secrets.put(playerUUID, secret);
            return secret;
        }
    }

    public void disconnectClient(UUID playerUUID) {
        connections.remove(playerUUID);
        secrets.remove(playerUUID);
    }

    public void close() {
        socket.close();
        processThread.close();
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

                    NetworkMessage.UnprocessedNetworkMessage msg = packetQueue.poll(10, TimeUnit.MILLISECONDS);
                    if (msg == null) {
                        continue;
                    }

                    NetworkMessage message = NetworkMessage.readPacketServer(msg, Server.this);

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
                            ClientConnection connection;
                            if (!connections.containsKey(packet.getPlayerUUID())) {
                                connection = new ClientConnection(packet.getPlayerUUID(), message.getAddress());
                                connections.put(packet.getPlayerUUID(), connection);
                                Voicechat.LOGGER.info("Successfully authenticated player {}", packet.getPlayerUUID());
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

                    if (message.getPacket() instanceof MicPacket) {
                        MicPacket packet = (MicPacket) message.getPacket();
                        Player player = server.getPlayer(playerUUID);
                        if (player == null) {
                            continue;
                        }
                        if (!player.hasPermission(VoiceChatCommands.SPEAK_PERMISSION)) {
                            CooldownTimer.run("muted-" + playerUUID, () -> {
                                player.sendMessage(Voicechat.translate("no_speak_permission"));
                            });
                            continue;
                        }
                        PlayerState state = playerStateManager.getState(playerUUID);
                        if (state != null) {
                            if (state.hasGroup()) {
                                processGroupPacket(state, packet);
                                if (Voicechat.SERVER_CONFIG.openGroups.get()) {
                                    processProximityPacket(state, player, packet);
                                }
                            } else {
                                processProximityPacket(state, player, packet);
                            }
                        }
                    } else if (message.getPacket() instanceof PingPacket) {
                        pingManager.onPongPacket((PingPacket) message.getPacket());
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

    private void processGroupPacket(PlayerState player, MicPacket packet) throws Exception {
        String group = player.getGroup();
        NetworkMessage soundMessage = new NetworkMessage(new SoundPacket(player.getGameProfile().getId(), packet.getData(), packet.getSequenceNumber()));
        for (PlayerState state : playerStateManager.getStates()) {
            if (!group.equals(state.getGroup())) {
                continue;
            }
            if (player.getGameProfile().getId().equals(state.getGameProfile().getId())) {
                continue;
            }
            ClientConnection connection = connections.get(state.getGameProfile().getId());
            if (connection != null) {
                connection.send(this, soundMessage);
            }
        }
    }

    private void processProximityPacket(PlayerState state, Player player, MicPacket packet) {
        double distance = Voicechat.SERVER_CONFIG.voiceChatDistance.get();
        String group = state.getGroup();

        NetworkMessage soundMessage = new NetworkMessage(new SoundPacket(player.getUniqueId(), packet.getData(), packet.getSequenceNumber()));

        ServerWorldUtils.getPlayersInRange(player.getWorld(), player.getLocation(), distance, p -> !p.getUniqueId().equals(player.getUniqueId()))
                .parallelStream()
                .map(p -> playerStateManager.getState(p.getUniqueId()))
                .filter(Objects::nonNull)
                .filter(s -> !s.isDisabled() && !s.isDisconnected()) // Filter out players that disabled the voice chat
                .filter(s -> !(s.hasGroup() && s.getGroup().equals(group))) // Filter out players that are in the same group
                .map(p -> connections.get(p.getGameProfile().getId()))
                .filter(Objects::nonNull)
                .forEach(clientConnection -> {
                    try {
                        clientConnection.send(this, soundMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
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

    public DatagramSocket getSocket() {
        return socket;
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
}