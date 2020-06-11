package de.maxhenkel.voicechat.voice.server;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class ClientConnection extends Thread {

    private static final long KEEP_ALIVE = 10_000L;

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private UUID playerUUID;
    private ArrayList<NetworkMessage> toSend;
    private boolean running;
    private long lastKeepAlive;

    public ClientConnection(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.toSend = new ArrayList<>();
        this.running = true;
        setDaemon(true);
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public int getPort() { //returns this de.maxhenkel.voice.client's tcp port
        return socket.getPort();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void addToQueue(NetworkMessage m) {
        try {
            toSend.add(m);
        } catch (Throwable t) {
        }
    }

    @Override
    public void run() {
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            try {
                socket.close();
                Main.LOGGER.error("ERROR " + getInetAddress() + ":" + getPort() + " " + e);
            } catch (IOException e1) {
            }
            return;
        }
        while (running) {
            try {
                if (socket.getInputStream().available() > 0) {
                    NetworkMessage incoming = NetworkMessage.readPacket(in, playerUUID);

                    if (incoming.getPacket() instanceof SoundPacket && playerUUID != null) {
                        server.sendMessageToNearby(incoming);
                    } else if (incoming.getPacket() instanceof AuthenticatePacket) {
                        AuthenticatePacket authenticatePacket = (AuthenticatePacket) incoming.getPacket();
                        if (server.getSecrets().get(authenticatePacket.getPlayerUUID()).equals(authenticatePacket.getSecret())) {
                            playerUUID = authenticatePacket.getPlayerUUID();
                            Main.LOGGER.info("Successfully authenticated " + playerUUID);
                            server.getSecrets().remove(authenticatePacket.getPlayerUUID());
                        } else {
                            Main.LOGGER.warn("Authentication for " + playerUUID + " failed... terminating");
                            close();
                        }
                    }

                }
                try {
                    if (!toSend.isEmpty()) {
                        NetworkMessage toClient = toSend.get(0);
                        toSend.remove(toClient);
                        if (toClient.getPacket() instanceof SoundPacket && (System.currentTimeMillis() - toClient.getTimestamp()) > toClient.getTTL()) {
                            Main.LOGGER.debug("Dropped packet from " + toClient.getPlayerUUID() + " to " + playerUUID);
                        } else {
                            toClient.send(out);
                        }
                    } else {
                        if (System.currentTimeMillis() - lastKeepAlive > KEEP_ALIVE) {
                            lastKeepAlive = System.currentTimeMillis();
                            new NetworkMessage(new KeepAlivePacket()).send(out);
                        }

                        Utils.sleep(10);
                    }
                } catch (Throwable t) {
                    if (t instanceof IOException) {
                        throw (Exception) t;
                    }
                    //TODO maybe fix
                }
            } catch (Exception ex) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                return;
            }
        }
    }

    public void close() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return !socket.isClosed();
    }
}
