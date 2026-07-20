package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.gui.onboarding.OnboardingManager;
import de.maxhenkel.voicechat.natives.ClientNativeManager;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerException;
import de.maxhenkel.voicechat.voice.common.NamedThreadPoolFactory;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class ClientVoicechat {

    @Nullable
    private volatile SoundManager soundManager;
    private final ExecutorService soundManagerExecutor;
    private final Map<UUID, AudioChannel> audioChannels;
    private final TalkCache talkCache;
    @Nullable
    private MicThread micThread;
    @Nullable
    private ClientVoicechatConnection connection;
    @Nullable
    private InitializationData initializationData;
    @Nullable
    private AudioRecorder recorder;
    private final long startTime;

    public ClientVoicechat() {
        this.startTime = System.currentTimeMillis();
        this.talkCache = new TalkCache();
        this.audioChannels = new HashMap<>();
        this.soundManagerExecutor = Executors.newSingleThreadExecutor(NamedThreadPoolFactory.create("VoicechatSoundManagerThread"));
        reloadSoundManager();
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
        initializationData = data;
        Voicechat.LOGGER.info("Connecting to voice chat server: '{}:{}'", initializationData.getServerIP(), initializationData.getServerPort());
        connection = new ClientVoicechatConnection(this, initializationData);
        connection.start();
        OnboardingManager.onConnecting();
        ClientNativeManager.onConnecting();
    }

    public void processSoundPacket(SoundPacket packet) {
        if (connection == null) {
            return;
        }
        synchronized (audioChannels) {
            if (!ClientManager.getPlayerStateManager().isDisabled()) {
                AudioChannel sendTo = audioChannels.get(packet.getChannelId());
                if (sendTo != null) {
                    sendTo.addToQueue(packet);
                } else {
                    SoundManager soundManager = getSoundManager();
                    if (soundManager != null) {
                        try {
                            AudioChannel ch = new AudioChannel(this, connection.getData(), soundManager, packet.getChannelId());
                            ch.addToQueue(packet);
                            ch.start();
                            audioChannels.put(packet.getChannelId(), ch);
                        } catch (Exception e) {
                            CooldownTimer.run("playback_unavailable", () -> {
                                Voicechat.LOGGER.error("Failed to create audio channel", e);
                                ChatUtils.sendModErrorMessage("message.voicechat.playback_unavailable", e);
                            });
                        }
                    }
                }
            }

            audioChannels.values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
            audioChannels.entrySet().removeIf(entry -> entry.getValue().isClosed());
        }
    }

    public void reloadSoundManager() {
        submitSoundManagerTask(() -> {
            closeSoundManager();
            try {
                soundManager = SoundManager.create();
            } catch (SpeakerException e) {
                Voicechat.LOGGER.error("Failed to start sound manager", e);
                Minecraft.getInstance().execute(() -> ChatUtils.sendModErrorMessage("message.voicechat.speaker_unavailable", e));
            }
        });
    }

    private void submitSoundManagerTask(Runnable task) {
        try {
            soundManagerExecutor.execute(task);
        } catch (RejectedExecutionException e) {
            Voicechat.LOGGER.debug("Ignoring sound manager task after shutdown");
        }
    }

    private void closeSoundManager() {
        if (soundManager != null) {
            soundManager.close();
            soundManager = null;
        }
    }

    public void reloadAudio() {
        Voicechat.LOGGER.info("Reloading audio");

        closeMicThread();

        synchronized (audioChannels) {
            Voicechat.LOGGER.info("Clearing audio channels");
            audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
            audioChannels.clear();
        }

        Voicechat.LOGGER.info("Restarting sound manager");
        reloadSoundManager();

        Voicechat.LOGGER.info("Starting microphone thread");
        if (connection != null) {
            startMicThread(connection);
        }
    }

    private void startMicThread(ClientVoicechatConnection connection) {
        if (micThread != null) {
            micThread.close();
        }
        micThread = new MicThread(this, connection, e -> {
            Voicechat.LOGGER.error("Failed to start microphone thread", e);
            ChatUtils.sendModErrorMessage("message.voicechat.microphone_unavailable", e);
        });
        micThread.start();
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
        LocalPlayer player = Minecraft.getInstance().player;
        if (recording) {
            if (connection == null || !connection.getData().allowRecording()) {
                if (player != null) {
                    player.displayClientMessage(new TranslatableComponent("message.voicechat.recording_disabled"), true);
                }
                return false;
            }
            recorder = AudioRecorder.create();
            if (player != null) {
                player.displayClientMessage(new TranslatableComponent("message.voicechat.recording_started").withStyle(ChatFormatting.DARK_RED), true);
            }
            return true;
        }

        AudioRecorder rec = recorder;
        recorder = null;
        if (player != null) {
            player.displayClientMessage(new TranslatableComponent("message.voicechat.recording_stopped").withStyle(ChatFormatting.DARK_RED), true);
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

    @Nullable
    public InitializationData getInitializationData() {
        return initializationData;
    }

    @Nullable
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

    public long getStartTime() {
        return startTime;
    }

    public Map<UUID, AudioChannel> getAudioChannels() {
        return audioChannels;
    }

    public boolean closeAudioChannel(UUID id) {
        synchronized (audioChannels) {
            boolean removed = audioChannels.remove(id) != null;
            if (removed) {
                Voicechat.LOGGER.debug("Removed audio channel of {} due to disconnection from voice chat", id);
            }
            return removed;
        }
    }

    public void close() {
        synchronized (audioChannels) {
            Voicechat.LOGGER.info("Clearing audio channels");
            audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
            audioChannels.clear();
        }

        submitSoundManagerTask(this::closeSoundManager);
        soundManagerExecutor.shutdown();
        try {
            if (!soundManagerExecutor.awaitTermination(1L, TimeUnit.SECONDS)) {
                Voicechat.LOGGER.warn("Timed out waiting for sound manager to close");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        closeMicThread();

        if (connection != null) {
            connection.close();
            connection = null;
        }

        if (recorder != null) {
            AudioRecorder rec = recorder;
            recorder = null;
            rec.saveAndClose();
        }
    }

}
