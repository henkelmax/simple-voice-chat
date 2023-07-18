package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerException;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientVoicechat {

    // @Nullable
    // private SoundManager soundManager;
    private final Map<UUID, AudioChannel> audioChannels;
    private final TalkCache talkCache;
    @Nullable
    private MicThread micThread;
    @Nullable
    private ClientVoicechatConnection connection;
    @Nullable
    private AudioRecorder recorder;
    private long startTime;

    public ClientVoicechat() {
        this.startTime = System.currentTimeMillis();
        this.talkCache = new TalkCache();
        try {
            reloadSoundManager();
        } catch (SpeakerException e) {
            Voicechat.LOGGER.error("Failed to start sound manager", e);
            ClientManager.sendPlayerError("message.voicechat.speaker_unavailable", e);
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

    public void connect(InitializationData data) throws Exception {
        Voicechat.LOGGER.info("Connecting to voice chat server: '{}:{}'", data.getServerIP(), data.getServerPort());
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
                        AudioChannel ch = new AudioChannel(this, connection.getData(), packet.getSender());
                        ch.addToQueue(packet);
                        ch.start();
                        audioChannels.put(packet.getSender(), ch);
                    } catch (Exception e) {
                        CooldownTimer.run("playback_unavailable", () -> {
                            Voicechat.LOGGER.error("Failed to create audio channel", e);
                            ClientManager.sendPlayerError("message.voicechat.playback_unavailable", e);
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
        // if (soundManager != null) {
        //     soundManager.close();
        // }
        // soundManager = new SoundManager(VoicechatClient.CLIENT_CONFIG.speaker.get());
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
            Voicechat.LOGGER.error("Failed to start microphone thread", e);
            ClientManager.sendPlayerError("message.voicechat.microphone_unavailable", e);
        }
    }

    public void closeMicThread() {
        if (micThread != null) {
            Voicechat.LOGGER.info("Stopping microphone thread");
            micThread.close();
            micThread = null;
        }
    }

    public boolean toggleRecording() {
        return setRecording(recorder == null);
    }

    public boolean setRecording(boolean recording) {
        if (recording && !VoicechatClient.CLIENT_CONFIG.useNatives.get()) {
            Voicechat.LOGGER.warn("Tried to start a recording with natives being disabled");
            return false;
        }

        if (recording == (recorder != null)) {
            return false;
        }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (recording) {
            if (connection == null || !connection.getData().allowRecording()) {
                if (player != null) {
                    player.sendStatusMessage(new TextComponentTranslation("message.voicechat.recording_disabled"), true);
                }
                return false;
            }
            recorder = AudioRecorder.create();
            if (player != null) {
                player.sendStatusMessage(new TextComponentTranslation("message.voicechat.recording_started").setStyle(new Style().setColor(TextFormatting.DARK_RED)), true);
            }
            return true;
        }

        AudioRecorder rec = recorder;
        recorder = null;
        if (player != null) {
            player.sendStatusMessage(new TextComponentTranslation("message.voicechat.recording_stopped").setStyle(new Style().setColor(TextFormatting.DARK_RED)), true);
        }
        rec.saveAndClose();
        return true;
    }

    @Nullable
    public MicThread getMicThread() {
        return micThread;
    }

    @Nullable
    public ClientVoicechatConnection getConnection() {
        return connection;
    }

    // @Nullable
    // public SoundManager getSoundManager() {
    //     return soundManager;
    // }

    public TalkCache getTalkCache() {
        return talkCache;
    }

    @Nullable
    public AudioRecorder getRecorder() {
        return recorder;
    }

    public long getStartTime() {
        return startTime;
    }

    public void close() {
        synchronized (audioChannels) {
            Voicechat.LOGGER.info("Clearing audio channels");
            audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
            audioChannels.clear();
        }

        // if (soundManager != null) {
        //     soundManager.close();
        // }

        closeMicThread();

        if (connection != null) {
            connection.close();
        }

        if (recorder != null) {
            AudioRecorder rec = recorder;
            recorder = null;
            rec.saveAndClose();
        }
    }

}
