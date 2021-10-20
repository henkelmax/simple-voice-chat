package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientVoicechat {

    private SoundManager soundManager;
    private final Map<UUID, AudioChannel> audioChannels;
    private final TalkCache talkCache;
    @Nullable
    private MicThread micThread;
    @Nullable
    private ClientVoicechatConnection connection;
    @Nullable
    private AudioRecorder recorder;

    public ClientVoicechat() {
        this.talkCache = new TalkCache();
        try {
            reloadSoundManager();
        } catch (SpeakerException e) {
            e.printStackTrace();
        }
        this.audioChannels = new HashMap<>();
    }

    public void onVoiceChatConnected(ClientVoicechatConnection connection) {
        startMicThread(connection);
    }

    public void onVoiceChatDisconnected() {
        closeMicThread();
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void connect(InitializationData data) throws IOException {
        Voicechat.LOGGER.info("Connecting to server: '" + data.getServerIP() + ":" + data.getServerPort() + "'");
        connection = new ClientVoicechatConnection(this, data);
        connection.start();
    }

    public void processSoundPacket(SoundPacket packet) {
        if (connection == null) {
            return;
        }
        synchronized (audioChannels) {
            if (!ClientManager.getPlayerStateManager().isDisabled()) {
                AudioChannel sendTo = audioChannels.get(packet.getSender());
                if (sendTo == null) {
                    try {
                        AudioChannel ch = new AudioChannel(this, connection, packet.getSender());
                        ch.addToQueue(packet);
                        ch.start();
                        audioChannels.put(packet.getSender(), ch);
                    } catch (NativeDependencyException e) {
                        CooldownTimer.run("decoder_unavailable", () -> {
                            Voicechat.LOGGER.error("Failed to create audio channel: {}", e.getMessage());
                            ClientManager.sendPlayerError("messsage.voicechat.playback_unavailable", e);
                        });
                    }
                } else {
                    sendTo.addToQueue(packet);
                }
            }

            audioChannels.values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
            audioChannels.entrySet().removeIf(entry -> entry.getValue().isClosed());
        }
    }

    public void reloadSoundManager() throws SpeakerException {
        if (soundManager != null) {
            soundManager.close();
        }
        soundManager = ClientCompatibilityManager.INSTANCE.createSoundManager(VoicechatClient.CLIENT_CONFIG.speaker.get());
    }

    public void reloadAudio() {
        Voicechat.LOGGER.info("Reloading audio");

        closeMicThread();

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
        if (connection != null) {
            startMicThread(connection);
        }
    }

    private void startMicThread(ClientVoicechatConnection connection) {
        if (micThread != null) {
            micThread.close();
        }
        try {
            micThread = new MicThread(this, connection);
            micThread.start();
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to start microphone thread: {}", e.getMessage());
            ClientManager.sendPlayerError("messsage.voicechat.microphone_unavailable", e);
        }
    }

    public void closeMicThread() {
        if (micThread != null) {
            Voicechat.LOGGER.info("Stopping microphone thread");
            micThread.close();
            micThread = null;
        }
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
            recorder = new AudioRecorder();
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

    @Nullable
    public MicThread getMicThread() {
        return micThread;
    }

    @Nullable
    public ClientVoicechatConnection getConnection() {
        return connection;
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

    public void close() {
        synchronized (audioChannels) {
            Voicechat.LOGGER.info("Clearing audio channels");
            audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
            audioChannels.clear();
        }

        soundManager.close();

        closeMicThread();

        if (connection != null) {
            connection.close();
        }

        if (recorder != null) {
            AudioRecorder rec = recorder;
            recorder = null;
            rec.close();
        }
    }

}
