package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.events.ClientVoiceChatEvents;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Client extends Thread {

    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private final UUID playerUUID;
    private final UUID secret;
    private final ServerConfig.Codec codec;
    private final int mtuSize;
    private final double voiceChatDistance;
    private final double voiceChatFadeDistance;
    private final int keepAlive;
    private final boolean groupsEnabled;
    private MicThread micThread;
    private boolean running;
    private final TalkCache talkCache;
    private boolean authenticated;
    private SoundManager soundManager;
    private final Map<UUID, AudioChannel> audioChannels;
    private final AuthThread authThread;
    private long lastKeepAlive;
    @Nullable
    private AudioRecorder recorder;
    private final boolean allowRecording;

    public Client(InitializationData data) throws IOException {
        this.address = InetAddress.getByName(data.getServerIP());
        this.port = data.getServerPort();
        this.socket = new DatagramSocket();
        this.socket.setTrafficClass(0x04); // IPTOS_RELIABILITY
        this.playerUUID = data.getPlayerUUID();
        this.secret = data.getSecret();
        this.codec = data.getCodec();
        this.mtuSize = data.getMtuSize();
        this.voiceChatDistance = data.getVoiceChatDistance();
        this.voiceChatFadeDistance = data.getVoiceChatFadeDistance();
        this.keepAlive = data.getKeepAlive();
        this.groupsEnabled = data.groupsEnabled();
        this.allowRecording = data.allowRecording();
        this.lastKeepAlive = -1;
        this.running = true;
        this.talkCache = new TalkCache();
        try {
            reloadSoundManager();
        } catch (SpeakerException e) {
            e.printStackTrace();
        }
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

    public ServerConfig.Codec getCodec() {
        return codec;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public boolean groupsEnabled() {
        return groupsEnabled;
    }

    public boolean allowRecording() {
        return allowRecording;
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

    public void reloadSoundManager() throws SpeakerException {
        if (soundManager != null) {
            soundManager.close();
        }
        soundManager = new SoundManager(VoicechatClient.CLIENT_CONFIG.speaker.get());
    }

    public void reloadAudio() {
        Voicechat.LOGGER.info("Reloading audio");

        if (micThread != null) {
            Voicechat.LOGGER.info("Stopping microphone thread");
            micThread.close();
            micThread = null;
        }

        synchronized (audioChannels) {
            Voicechat.LOGGER.info("Clearing audio channels");
            audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
            audioChannels.clear();
            try {
                Voicechat.LOGGER.info("Restarting sound manager");
                reloadSoundManager();
            } catch (SpeakerException e) {
                e.printStackTrace();
            }
        }

        Voicechat.LOGGER.info("Starting microphone thread");
        startMicThread();
    }

    private void startMicThread() {
        if (micThread != null) {
            micThread.close();
        }
        try {
            micThread = new MicThread(this);
            micThread.start();
        } catch (Exception e) {
            Voicechat.LOGGER.error("Microphone unavailable: {}", e.getMessage());
            sendPlayerError("messsage.voicechat.microphone_unavailable", e);
        }
    }

    public void sendPlayerError(String translationKey, Exception e) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.sendMessage(
                ComponentUtils.wrapInSquareBrackets(new TextComponent(Voicechat.getModName()))
                        .withStyle(ChatFormatting.GREEN)
                        .append(" ")
                        .append(new TranslatableComponent(translationKey).withStyle(ChatFormatting.RED))
                        .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(e.getMessage()).withStyle(ChatFormatting.RED))))
                , Util.NIL_UUID);
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
                        ClientVoiceChatEvents.VOICECHAT_CONNECTED.invoker().accept(this);
                        startMicThread();
                        lastKeepAlive = System.currentTimeMillis();
                    }
                } else if (in.getPacket() instanceof SoundPacket packet) {
                    synchronized (audioChannels) {
                        if (!VoicechatClient.CLIENT.getPlayerStateManager().isDisabled()) {
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
                    }
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

        synchronized (audioChannels) {
            Voicechat.LOGGER.info("Clearing audio channels");
            audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
            audioChannels.clear();
        }

        socket.close();
        authThread.close();
        soundManager.close();

        if (micThread != null) {
            micThread.close();
        }

        if (recorder != null) {
            AudioRecorder rec = recorder;
            recorder = null;
            rec.close();
        }
    }

    @Nullable
    public MicThread getMicThread() {
        return micThread;
    }

    public boolean isConnected() {
        return running && !socket.isClosed();
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public TalkCache getTalkCache() {
        return talkCache;
    }

    @Nullable
    public AudioRecorder getRecorder() {
        return recorder;
    }

    public void toggleRecording() {
        setRecording(recorder == null);
    }

    public void setRecording(boolean recording) {
        if (recording == (recorder != null)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (recording) {
            recorder = new AudioRecorder(this);
            if (player != null) {
                player.displayClientMessage(new TranslatableComponent("message.voicechat.recording_started").withStyle(ChatFormatting.DARK_RED), true);
            }
        } else {
            AudioRecorder rec = recorder;
            recorder = null;
            if (player != null) {
                player.displayClientMessage(new TranslatableComponent("message.voicechat.recording_stopped").withStyle(ChatFormatting.DARK_RED), true);
            }
            rec.save();
        }
    }

    public void sendToServer(NetworkMessage message) throws Exception {
        byte[] data = message.writeClient(this);
        socket.send(new DatagramPacket(data, data.length, address, port));
    }

    public void checkTimeout() {
        if (lastKeepAlive >= 0 && System.currentTimeMillis() - lastKeepAlive > keepAlive * 10L) {
            Voicechat.LOGGER.info("Connection timeout");
            VoicechatClient.CLIENT.onDisconnect();
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
                    sendToServer(new NetworkMessage(new AuthenticatePacket(playerUUID, secret)));
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