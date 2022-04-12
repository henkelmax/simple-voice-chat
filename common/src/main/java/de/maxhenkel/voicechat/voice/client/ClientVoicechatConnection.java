package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.common.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientVoicechatConnection extends Thread {

    private ClientVoicechat client;
    private final InitializationData data;
    private final DatagramSocket socket;
    private final InetAddress address;
    private boolean running;
    private boolean authenticated;
    private final AuthThread authThread;
    private long lastKeepAlive;

    public ClientVoicechatConnection(ClientVoicechat client, InitializationData data) throws IOException {
        this.client = client;
        this.data = data;
        this.address = InetAddress.getByName(data.getServerIP());
        this.socket = new DatagramSocket();
        this.socket.setTrafficClass(0x04); // IPTOS_RELIABILITY
        this.lastKeepAlive = -1;
        this.running = true;
        this.authThread = new AuthThread();
        this.authThread.start();
        setDaemon(true);
        setName("VoiceChatConnectionThread");
    }

    public InitializationData getData() {
        return data;
    }

    public InetAddress getAddress() {
        return address;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void run() {
        try {
            while (running) {
                NetworkMessage in = NetworkMessage.readPacketClient(socket, this);
                if (in.getPacket() instanceof AuthenticateAckPacket) {
                    if (!authenticated) {
                        Voicechat.LOGGER.info("Server acknowledged authentication");
                        authenticated = true;
                        ClientCompatibilityManager.INSTANCE.emitVoiceChatConnectedEvent(this);
                        lastKeepAlive = System.currentTimeMillis();
                    }
                } else if (in.getPacket() instanceof SoundPacket packet) {
                    client.processSoundPacket(packet);
                } else if (in.getPacket() instanceof PingPacket packet) {
                    Voicechat.LOGGER.info("Received ping {}, sending pong...", packet.getId());
                    sendToServer(new NetworkMessage(packet));
                } else if (in.getPacket() instanceof KeepAlivePacket) {
                    lastKeepAlive = System.currentTimeMillis();
                    sendToServer(new NetworkMessage(new KeepAlivePacket()));
                }
            }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            if (running) {
                Voicechat.LOGGER.error("Failed to process packet from server: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void close() {
        Voicechat.LOGGER.info("Disconnecting voicechat");
        running = false;

        socket.close();
        authThread.close();
    }

    public boolean isConnected() {
        return running && !socket.isClosed();
    }

    public void sendToServer(NetworkMessage message) throws Exception {
        if (!isConnected()) {
            return; // Ignore sending packets when connection is closed
        }
        byte[] bytes = message.writeClient(this);
        socket.send(new DatagramPacket(bytes, bytes.length, address, data.getServerPort()));
    }

    public void checkTimeout() {
        if (lastKeepAlive >= 0 && System.currentTimeMillis() - lastKeepAlive > data.getKeepAlive() * 10L) {
            Voicechat.LOGGER.info("Connection timeout");
            ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
        }
    }

    private class AuthThread extends Thread {
        private boolean running;

        public AuthThread() {
            this.running = true;
            setDaemon(true);
            setName("VoiceChatAuthenticationThread");
        }

        @Override
        public void run() {
            while (running && !authenticated) {
                try {
                    Voicechat.LOGGER.info("Trying to authenticate voice connection");
                    sendToServer(new NetworkMessage(new AuthenticatePacket(data.getPlayerUUID(), data.getSecret())));
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        Voicechat.LOGGER.error("Failed to authenticate voice connection: {}", e.getMessage());
                    }
                }
                Utils.sleep(1000);
            }
        }

        public void close() {
            running = false;
        }
    }

}