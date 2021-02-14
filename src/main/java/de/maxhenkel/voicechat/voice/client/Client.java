package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.voice.common.*;
import de.maxhenkel.voicechat.Voicechat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Client extends Thread {

    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private UUID playerUUID;
    private UUID secret;
    private int sampleRate;
    private double voiceChatDistance;
    private double voiceChatFadeDistance;
    private MicThread micThread;
    private boolean running;
    private TalkCache talkCache;
    private boolean authenticated;
    private Map<UUID, AudioChannel> audioChannels;
    private AuthThread authThread;
    private boolean muted;
    private AudioChannelConfig audioChannelConfig;

    public Client(String serverIp, int serverPort, UUID playerUUID, UUID secret, int sampleRate, double voiceChatDistance, double voiceChatFadeDistance) throws IOException {
        this.address = InetAddress.getByName(serverIp);
        this.port = serverPort;
        this.socket = new DatagramSocket();
        this.socket.setTrafficClass(0x04); // IPTOS_RELIABILITY
        this.playerUUID = playerUUID;
        this.secret = secret;
        this.sampleRate = sampleRate;
        this.voiceChatDistance = voiceChatDistance;
        this.voiceChatFadeDistance = voiceChatFadeDistance;
        this.running = true;
        this.talkCache = new TalkCache();
        this.audioChannels = new HashMap<>();
        this.authThread = new AuthThread();
        this.authThread.start();
        this.audioChannelConfig = new AudioChannelConfig(this);
        setDaemon(true);
        setName("VoiceChatClientThread");
    }

    public AudioChannelConfig getAudioChannelConfig() {
        return audioChannelConfig;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public UUID getSecret() {
        return secret;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public double getVoiceChatDistance() {
        return voiceChatDistance;
    }

    public double getVoiceChatFadeDistance() {
        return voiceChatFadeDistance;
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

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public void reloadDataLines() {
        Voicechat.LOGGER.debug("Reloading data lines");
        if (micThread != null) {
            Voicechat.LOGGER.debug("Restarting microphone thread");
            micThread.close();
            micThread = null;
            startMicThread();
        }
        Voicechat.LOGGER.debug("Clearing audio channels");
        audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
        audioChannels.clear();
    }

    private void startMicThread() {
        try {
            micThread = new MicThread(this);
            micThread.start();
        } catch (Exception e) {
            Voicechat.LOGGER.error("Mic unavailable " + e);
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                NetworkMessage in = NetworkMessage.readPacket(socket);
                if (in.getPacket() instanceof AuthenticateAckPacket) {
                    if (!authenticated) {
                        Voicechat.LOGGER.info("Server acknowledged authentication");
                        authenticated = true;
                        startMicThread();
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
                } else if (in.getPacket() instanceof PingPacket) {
                    PingPacket packet = (PingPacket) in.getPacket();
                    Voicechat.LOGGER.debug("Received ping {}, sending pong...", packet.getId());
                    new NetworkMessage(packet, secret).sendToServer(this);
                }
            }
        } catch (Exception e) {
            if (running) {
                Voicechat.LOGGER.error("Failed to process packet from server: {}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void close() {
        Voicechat.LOGGER.info("Disconnecting client");
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
                    Voicechat.LOGGER.info("Trying to authenticate voice connection");
                    new NetworkMessage(new AuthenticatePacket(playerUUID, secret)).sendToServer(Client.this);
                } catch (IOException e) {
                    Voicechat.LOGGER.error("Failed to authenticate voice connection: {}", e.getMessage());
                }
                Utils.sleep(1000);
            }
        }

        public void close() {
            running = false;
        }
    }

}
 