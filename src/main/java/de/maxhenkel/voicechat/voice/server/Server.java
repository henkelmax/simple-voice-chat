package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;

import java.net.DatagramSocket;
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
    private List<NetworkMessage> sendQueue;

    public Server(int port, MinecraftServer server) {
        this.port = port;
        this.server = server;
        connections = new HashMap<>();
        secrets = new HashMap<>();
        sendQueue = new ArrayList<>();
        setDaemon(true);
        setName("VoiceChatServerThread");
        processThread = new ProcessThread();
        processThread.start();
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(port);
            Main.LOGGER.info("Server started at port " + port);

            while (!socket.isClosed()) {
                try {
                    NetworkMessage message = NetworkMessage.readPacket(socket);
                    sendQueue.add(message);
                } catch (Exception e) {
                    e.printStackTrace();
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
                    if (sendQueue.isEmpty()) {
                        Utils.sleep(10);
                    } else {
                        NetworkMessage message = sendQueue.get(0);
                        sendQueue.remove(message);
                        if (System.currentTimeMillis() - message.getTimestamp() > message.getTTL()) {
                            continue;
                        }

                        if (message.getPacket() instanceof AuthenticatePacket) {
                            AuthenticatePacket packet = (AuthenticatePacket) message.getPacket();
                            UUID secret = secrets.get(packet.getPlayerUUID());
                            if (secret != null && secret.equals(packet.getSecret())) {
                                ClientConnection connection;
                                if (!connections.containsKey(message.getPlayerUUID())) {
                                    connection = new ClientConnection(message.getPlayerUUID(), message.getAddress(), message.getPort());
                                    connections.put(message.getPlayerUUID(), connection);
                                    Main.LOGGER.info("Successfully authenticated player {}", message.getPlayerUUID());
                                } else {
                                    connection = connections.get(message.getPlayerUUID());
                                }
                                new NetworkMessage(new AuthenticateAckPacket(), message.getPlayerUUID(), packet.getSecret()).sendTo(socket, connection);
                            }
                        } else if (message.getPacket() instanceof SoundPacket) {
                            ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(message.getPlayerUUID());
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
                            for (ClientConnection clientConnection : closeConnections) {
                                if (!clientConnection.getPlayerUUID().equals(message.getPlayerUUID())) {
                                    message.sendTo(socket, clientConnection);
                                }
                            }
                        }
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

}
