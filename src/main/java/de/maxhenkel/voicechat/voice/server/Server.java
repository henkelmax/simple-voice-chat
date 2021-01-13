package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

public class Server extends Thread {

    private Map<UUID, ClientConnection> connections;
    private Map<UUID, UUID> secrets;
    private int port;
    private MinecraftServer server;
    private DatagramSocket socket;
    private ProcessThread processThread;
    private List<NetworkMessage> packetQueue;
    private PingManager pingManager;

    public Server(int port, MinecraftServer server) {
        this.port = port;
        this.server = server;
        connections = new HashMap<>();
        secrets = new HashMap<>();
        packetQueue = new ArrayList<>();
        pingManager = new PingManager(this);
        setDaemon(true);
        setName("VoiceChatServerThread");
        processThread = new ProcessThread();
        processThread.start();
    }

    @Override
    public void run() {
        try {
            InetAddress address = null;
            String addr = Main.SERVER_CONFIG.voiceChatBindAddress.get();
            try {
                if (!addr.isEmpty()) {
                    address = InetAddress.getByName(addr);
                }
            } catch (Exception e) {
                Main.LOGGER.error("Failed to parse bind IP address '" + addr + "'");
                Main.LOGGER.info("Binding to default IP address");
                e.printStackTrace();
            }
            try {
                socket = new DatagramSocket(port, address);
            } catch (BindException e) {
                Main.LOGGER.error("Failed to bind to address '" + addr + "'");
                e.printStackTrace();
                System.exit(1);
                return;
            }
            Main.LOGGER.info("Server started at port " + port);

            while (!socket.isClosed()) {
                try {
                    NetworkMessage message = NetworkMessage.readPacket(socket);
                    packetQueue.add(message);
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

        public ProcessThread() {
            this.running = true;
            setDaemon(true);
            setName("VoiceChatPacketProcessingThread");
        }

        @Override
        public void run() {
            while (running) {
                try {
                    pingManager.checkTimeouts();
                    if (packetQueue.isEmpty()) {
                        Utils.sleep(10);
                        continue;
                    }

                    NetworkMessage message = packetQueue.get(0);
                    packetQueue.remove(message);
                    if (System.currentTimeMillis() - message.getTimestamp() > message.getTTL()) {
                        continue;
                    }

                    if (message.getPacket() instanceof AuthenticatePacket) {
                        AuthenticatePacket packet = (AuthenticatePacket) message.getPacket();
                        UUID secret = secrets.get(packet.getPlayerUUID());
                        if (secret != null && secret.equals(packet.getSecret())) {
                            ClientConnection connection;
                            if (!connections.containsKey(packet.getPlayerUUID())) {
                                connection = new ClientConnection(packet.getPlayerUUID(), message.getAddress(), message.getPort());
                                connections.put(packet.getPlayerUUID(), connection);
                                Main.LOGGER.info("Successfully authenticated player {}", packet.getPlayerUUID());
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

                    if (!isPacketAuthorized(message, playerUUID)) {
                        continue;
                    }

                    if (message.getPacket() instanceof MicPacket) {
                        MicPacket packet = (MicPacket) message.getPacket();
                        ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(playerUUID);
                        if (player == null) {
                            continue;
                        }
                        double distance = Main.SERVER_CONFIG.voiceChatDistance.get();
                        List<ClientConnection> closeConnections = player.world
                                .getEntitiesWithinAABB(
                                        PlayerEntity.class,
                                        new AxisAlignedBB(
                                                player.getPosX() - distance,
                                                player.getPosY() - distance,
                                                player.getPosZ() - distance,
                                                player.getPosX() + distance,
                                                player.getPosY() + distance,
                                                player.getPosZ() + distance
                                        )
                                        , playerEntity -> !playerEntity.getUniqueID().equals(player.getUniqueID())
                                )
                                .stream()
                                .map(playerEntity -> connections.get(playerEntity.getUniqueID()))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        NetworkMessage soundMessage = new NetworkMessage(new SoundPacket(playerUUID, packet.getData()));
                        for (ClientConnection clientConnection : closeConnections) {
                            if (!clientConnection.getPlayerUUID().equals(playerUUID)) {
                                soundMessage.sendTo(socket, clientConnection);
                            }
                        }
                    } else if (message.getPacket() instanceof PingPacket) {
                        pingManager.onPongPacket((PingPacket) message.getPacket());
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

    private boolean isPacketAuthorized(NetworkMessage message, UUID sender) {
        UUID secret = secrets.get(sender);
        return secret != null && secret.equals(message.getSecret());
    }

    public Map<UUID, ClientConnection> getConnections() {
        return connections;
    }

    public Map<UUID, UUID> getSecrets() {
        return secrets;
    }

    public void sendPacket(Packet<?> packet, ClientConnection connection) throws IOException {
        new NetworkMessage(packet).sendTo(socket, connection);
    }

    public PingManager getPingManager() {
        return pingManager;
    }
}
