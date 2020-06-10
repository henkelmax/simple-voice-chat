package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Config;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Server extends Thread {

    private Map<UUID, UUID> secrets; //TODO clean up
    private List<NetworkMessage> sendQueue;
    private List<ClientConnection> connections;
    private ServerSocket serverSocket;
    private BroadcastThread broadcastThread;
    private int port;
    private MinecraftServer server;

    public Server(int port, MinecraftServer server) {
        this.secrets = new HashMap<>();
        this.connections = new ArrayList<>();
        this.sendQueue = new ArrayList<>();
        this.port = port;
        this.server = server;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Main.LOGGER.info("Server started at port " + port);
        broadcastThread = new BroadcastThread();
        broadcastThread.start();
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                ClientConnection clientConnection = new ClientConnection(this, socket);
                clientConnection.start();
                addConnection(clientConnection);
                Main.LOGGER.info("New client " + socket.getInetAddress() + ":" + socket.getPort() + " on port " + port);
            } catch (IOException ex) {
            }
        }
    }

    public void sendMessageToNearby(NetworkMessage m) {
        try {
            sendQueue.add(m);
        } catch (Throwable t) {
            Utils.sleep(1);
            sendMessageToNearby(m);
        }
    }

    private void addConnection(ClientConnection clientConnection) {
        try {
            connections.add(clientConnection);
        } catch (Throwable t) {
            Utils.sleep(1);
            addConnection(clientConnection);
        }
    }

    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (broadcastThread != null) {
            broadcastThread.close();
        }
        for (ClientConnection clientConnection : connections) {
            clientConnection.close();
        }
    }

    private class BroadcastThread extends Thread {
        private boolean running;

        public BroadcastThread() {
            this.running = true;
            setDaemon(true);
        }

        @Override
        public void run() {
            while (running) {
                try {
                    connections.forEach(clientConnection -> {
                        if (!clientConnection.isConnected()) {
                            Main.LOGGER.info("Disconnecting client " + clientConnection.getPlayerUUID());
                        }
                    });
                    connections.removeIf(clientConnection -> !clientConnection.isConnected());
                    if (sendQueue.isEmpty()) {
                        Utils.sleep(10);
                    } else {
                        //TODO only for audio packets
                        NetworkMessage message = sendQueue.get(0);
                        ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(message.getPlayerUUID());
                        if (player == null) {
                            Utils.sleep(10);
                            continue;
                        }
                        double distance = Config.SERVER.VOICE_CHAT_DISTANCE.get();
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
                                .map(playerEntity -> getConnectionFromUUID(playerEntity.getUniqueID()))
                                .collect(Collectors.toList());
                        for (ClientConnection clientConnection : closeConnections) {
                            if (!clientConnection.getPlayerUUID().equals(message.getPlayerUUID())) {
                                clientConnection.addToQueue(message);
                            }
                        }
                        sendQueue.remove(message);
                    }
                } catch (Throwable t) {
                }
            }
        }

        public void close() {
            running = false;
        }
    }

    public ClientConnection getConnectionFromUUID(UUID uuid) {
        return connections.stream().filter(clientConnection -> clientConnection.getPlayerUUID().equals(uuid)).findFirst().orElse(null);
    }

    public Map<UUID, UUID> getSecrets() {
        return secrets;
    }
}
