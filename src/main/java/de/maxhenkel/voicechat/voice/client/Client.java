package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

public class Client extends Thread {

    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private UUID playerUUID;
    private UUID secret;
    private MicThread micThread;
    private boolean running;
    private TalkCache talkCache;
    private boolean authenticated;
    private Map<UUID, AudioChannel> audioChannels;
    private AuthThread authThread;

    public Client(String serverIp, int serverPort, UUID playerUUID, UUID secret) throws IOException {
        this.address = InetAddress.getByName(serverIp);
        this.port = serverPort;
        this.socket = new DatagramSocket();
        this.playerUUID = playerUUID;
        this.secret = secret;
        this.running = true;
        this.talkCache = new TalkCache();
        this.audioChannels = new HashMap<>();
        this.authThread = new AuthThread();
        this.authThread.start();
        setDaemon(true);
        setName("VoiceChatClientThread");
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getSecret() {
        return secret;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try {
            while (running) {
                NetworkMessage in = NetworkMessage.readPacket(socket);
                if (in.getPacket() instanceof AuthenticateAckPacket) {
                    if (!authenticated) {
                        Main.LOGGER.info("Server acknowledged authentication");
                        authenticated = true;

                        try {
                            micThread = new MicThread(this);
                            micThread.start();
                        } catch (Exception e) {
                            Main.LOGGER.error("Mic unavailable " + e);
                        }
                    }
                } else if (in.getPacket() instanceof SoundPacket) {
                    SoundPacket packet = (SoundPacket) in.getPacket();
                    AudioChannel sendTo = audioChannels.get(packet.getSender());
                    if (sendTo == null) {
                        AudioChannel ch = new AudioChannel(this, packet.getSender());
                        ch.addToQueue(in);
                        ch.start();
                        audioChannels.put(packet.getSender(), ch);
                    } else {
                        sendTo.addToQueue(in);
                    }

                    audioChannels.values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
                    audioChannels.entrySet().removeIf(entry -> entry.getValue().isClosed());
                }

            }
        } catch (Exception e) {
            if (running) {
                Main.LOGGER.error("Failed to process packet from server: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void close() {
        Main.LOGGER.info("Disconnecting client");
        running = false;
        socket.close();
        authThread.close();

        if (micThread != null) {
            micThread.close();
        }
    }

    public MicThread getMicThread() {
        return micThread;
    }

    public boolean isConnected() {
        return running && !socket.isClosed();
    }

    public TalkCache getTalkCache() {
        return talkCache;
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
                    Main.LOGGER.info("Trying to authenticate voice connection");
                    new NetworkMessage(new AuthenticatePacket(playerUUID, secret)).sendToServer(Client.this);
                } catch (IOException e) {
                    Main.LOGGER.error("Failed to authenticate voice connection: {}", e.getMessage());
                }
                Utils.sleep(1000);
            }
        }

        public void close() {
            running = false;
        }
    }

}
 