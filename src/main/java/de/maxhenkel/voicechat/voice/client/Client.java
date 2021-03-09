package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.event.VoiceChatConnectedEvent;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraftforge.common.MinecraftForge;
import org.jline.utils.Log;

import java.io.IOException;
import java.net.DatagramPacket;
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
    private long sequenceNumber;
    private long lastServerSequenceNumber;
    private long lastKeepAlive;

    public Client(String serverIp, int serverPort, UUID playerUUID, UUID secret) throws IOException {
        this.address = InetAddress.getByName(serverIp);
        this.port = serverPort;
        this.socket = new DatagramSocket();
        this.socket.setTrafficClass(0x04); // IPTOS_RELIABILITY
        this.playerUUID = playerUUID;
        this.secret = secret;
        this.sequenceNumber = 0L;
        this.lastServerSequenceNumber = -1L;
        this.lastKeepAlive = -1L;
        this.running = true;
        this.talkCache = new TalkCache();
        this.audioChannels = new HashMap<>();
        this.authThread = new AuthThread();
        this.authThread.start();
        setDaemon(true);
        setName("VoiceChatClientThread");
    }

    public long getAndIncreaseSequenceNumber() {
        long num = sequenceNumber;
        sequenceNumber++;
        return num;
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

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void reloadDataLines() {
        Log.debug("Reloading data lines");
        if (micThread != null) {
            Log.debug("Restarting microphone thread");
            micThread.close();
            micThread = null;
            startMicThread();
        }
        Log.debug("Clearing audio channels");
        audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
        audioChannels.clear();
    }

    private void startMicThread() {
        try {
            micThread = new MicThread(this);
            micThread.start();
        } catch (Exception e) {
            Main.LOGGER.error("Mic unavailable " + e);
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                NetworkMessage in = NetworkMessage.readPacket(socket);
                if (in.getSequenceNumber() <= lastServerSequenceNumber) {
                    continue;
                }
                lastServerSequenceNumber = in.getSequenceNumber();
                if (in.getPacket() instanceof AuthenticateAckPacket) {
                    if (!authenticated) {
                        Main.LOGGER.info("Server acknowledged authentication");
                        authenticated = true;
                        MinecraftForge.EVENT_BUS.post(new VoiceChatConnectedEvent(this));
                        startMicThread();
                        lastKeepAlive = System.currentTimeMillis();
                    }
                } else if (in.getPacket() instanceof SoundPacket) {
                    if (!Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().isDisabled()) {
                        SoundPacket packet = (SoundPacket) in.getPacket();
                        AudioChannel sendTo = audioChannels.get(packet.getSender());
                        if (sendTo == null) {
                            AudioChannel ch = new AudioChannel(this, packet.getSender());
                            ch.addToQueue(packet);
                            ch.start();
                            audioChannels.put(packet.getSender(), ch);
                        } else {
                            sendTo.addToQueue(packet);
                        }
                    }

                    audioChannels.values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
                    audioChannels.entrySet().removeIf(entry -> entry.getValue().isClosed());
                } else if (in.getPacket() instanceof PingPacket) {
                    PingPacket packet = (PingPacket) in.getPacket();
                    Main.LOGGER.info("Received ping {}, sending pong...", packet.getId());
                    sendToServer(new NetworkMessage(packet, secret));
                } else if (in.getPacket() instanceof KeepAlivePacket) {
                    lastKeepAlive = System.currentTimeMillis();
                    sendToServer(new NetworkMessage(new KeepAlivePacket(), secret));
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

    public void sendToServer(NetworkMessage message) throws IOException {
        byte[] data = message.write(getAndIncreaseSequenceNumber());
        socket.send(new DatagramPacket(data, data.length, address, port));
    }

    public void checkTimeout() {
        if (lastKeepAlive >= 0 && System.currentTimeMillis() - lastKeepAlive > Main.SERVER_CONFIG.keepAlive.get() * 10L) {
            Main.LOGGER.info("Connection timeout");
            Main.CLIENT_VOICE_EVENTS.onDisconnect();
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
                    Main.LOGGER.info("Trying to authenticate voice connection");
                    sendToServer(new NetworkMessage(new AuthenticatePacket(playerUUID, secret)));
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        Main.LOGGER.error("Failed to authenticate voice connection: {}", e.getMessage());
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
