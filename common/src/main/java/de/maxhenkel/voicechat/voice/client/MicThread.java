package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusManager;
import de.maxhenkel.voicechat.voice.client.microphone.Microphone;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import de.maxhenkel.voicechat.voice.common.MicPacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class MicThread extends Thread {

    @Nullable
    private final ClientVoicechat client;
    @Nullable
    private final ClientVoicechatConnection connection;
    @Nullable
    private final Microphone mic;
    private final VolumeManager volumeManager;
    private boolean running;
    private boolean microphoneLocked;
    private boolean wasWhispering;
    private final OpusEncoder encoder;
    @Nullable
    private Denoiser denoiser;

    public MicThread(@Nullable ClientVoicechat client, @Nullable ClientVoicechatConnection connection) throws MicrophoneException {
        this.client = client;
        this.connection = connection;
        this.running = true;
        this.encoder = OpusManager.createEncoder(connection == null ? ServerConfig.Codec.VOIP.getMode() : connection.getData().getCodec().getMode());

        this.denoiser = Denoiser.createDenoiser();
        if (denoiser == null) {
            Voicechat.LOGGER.warn("Denoiser not available");
        }
        volumeManager = new VolumeManager();

        setDaemon(true);
        setName("MicrophoneThread");
        setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());

        mic = MicrophoneManager.createMicrophone();
    }

    @Override
    public void run() {
        while (running) {
            if (connection != null) {
                // Checking here for timeouts, because we don't have any other looping thread
                connection.checkTimeout();
                if (!running) {
                    break;
                }
            }
            if (microphoneLocked || ClientManager.getPlayerStateManager().isDisabled()) {
                activating = false;
                wasPTT = false;
                wasWhispering = false;
                flushIfNeeded();

                if (!microphoneLocked && ClientManager.getPlayerStateManager().isDisabled()) {
                    if (mic.isStarted()) {
                        mic.stop();
                    }
                    if (denoiser != null) {
                        denoiser.close();
                    }
                }

                Utils.sleep(10);
                continue;
            }

            short[] audio = pollMic();
            if (audio == null) {
                continue;
            }

            boolean sentAudio = false;
            MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
            if (type.equals(MicrophoneActivationType.PTT)) {
                sentAudio = ptt(audio);
            } else if (type.equals(MicrophoneActivationType.VOICE)) {
                sentAudio = voice(audio);
            }
            if (!sentAudio) {
                sendAudio(null, ClientManager.getPttKeyHandler().isWhisperDown());
            }
        }
    }

    @Nullable
    public short[] pollMic() {
        if (!mic.isStarted()) {
            mic.start();
        }
        if (denoiser != null && denoiser.isClosed()) {
            denoiser = Denoiser.createDenoiser();
        }

        if (mic.available() < SoundManager.FRAME_SIZE) {
            Utils.sleep(5);
            return null;
        }
        short[] buff = mic.read();
        volumeManager.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());
        return denoiseIfEnabled(buff);
    }

    private volatile boolean activating;
    private volatile int deactivationDelay;
    private volatile short[] lastBuff;

    private boolean voice(short[] audio) {
        wasPTT = false;

        if (ClientManager.getPlayerStateManager().isMuted()) {
            activating = false;
            wasWhispering = false;
            return false;
        }

        boolean sentAudio = false;

        wasWhispering = ClientManager.getPttKeyHandler().isWhisperDown();

        int offset = Utils.getActivationOffset(audio, VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get());
        if (activating) {
            if (offset < 0) {
                if (deactivationDelay >= VoicechatClient.CLIENT_CONFIG.deactivationDelay.get()) {
                    activating = false;
                    deactivationDelay = 0;
                } else {
                    sendAudio(audio, wasWhispering);
                    deactivationDelay++;
                    sentAudio = true;
                }
            } else {
                sendAudio(audio, wasWhispering);
                sentAudio = true;
            }
        } else {
            if (offset > 0) {
                if (lastBuff != null) {
                    sendAudio(lastBuff, wasWhispering);
                }
                sendAudio(audio, wasWhispering);
                activating = true;
                sentAudio = true;
            }
        }
        lastBuff = audio;
        return sentAudio;
    }

    private volatile boolean wasPTT;

    private boolean ptt(short[] audio) {
        activating = false;
        if (!ClientManager.getPttKeyHandler().isAnyDown()) {
            if (wasPTT) {
                wasPTT = false;
                wasWhispering = false;
            }
            return false;
        }
        wasPTT = true;
        wasWhispering = ClientManager.getPttKeyHandler().isWhisperDown();
        sendAudio(audio, wasWhispering);
        return true;
    }

    public short[] denoiseIfEnabled(short[] audio) {
        if (denoiser != null && VoicechatClient.CLIENT_CONFIG.denoiser.get()) {
            return denoiser.denoise(audio);
        }
        return audio;
    }

    private void flush() {
        sendStopPacket();
        if (!encoder.isClosed()) {
            encoder.resetState();
        }
        if (client == null) {
            return;
        }
        AudioRecorder recorder = client.getRecorder();
        if (recorder == null) {
            return;
        }
        recorder.flushChunkThreaded(Minecraft.getMinecraft().getSession().getProfile().getId());
    }

    private boolean hasSentAudio;

    /**
     * Sends the audio to the server if necessary.
     * If {@param rawAudio} is null and no audio is being injected, a stop packet will be sent.
     * This needs to get called every microphone poll, even if no mic audio should be sent.
     *
     * @param rawAudio   the raw audio or
     * @param whispering whether the player is whispering
     */
    private void sendAudio(@Nullable short[] rawAudio, boolean whispering) {
        @Nullable short[] mergedAudio = PluginManager.instance().onMergeClientSound(rawAudio);
        if (mergedAudio == null) {
            flushIfNeeded();
            return;
        }
        short[] finalAudio = PluginManager.instance().onClientSound(mergedAudio, whispering);
        if (finalAudio == null) {
            flushIfNeeded();
            return;
        }

        sendAudioPacket(finalAudio, whispering);
        hasSentAudio = true;
    }

    private void flushIfNeeded() {
        if (!hasSentAudio) {
            return;
        }
        flush();
        hasSentAudio = false;
    }

    public boolean isTalking() {
        return !microphoneLocked && (activating || wasPTT);
    }

    public boolean isWhispering() {
        return isTalking() && wasWhispering;
    }

    public void setMicrophoneLocked(boolean microphoneLocked) {
        this.microphoneLocked = microphoneLocked;
        activating = false;
        wasPTT = false;
        deactivationDelay = 0;
        lastBuff = null;
    }

    public void close() {
        if (!running) {
            return;
        }
        running = false;

        if (Thread.currentThread() != this) {
            try {
                join(100);
            } catch (InterruptedException e) {
                Voicechat.LOGGER.error("Interrupted while waiting for mic thread to close", e);
            }
        }

        if (mic != null) {
            mic.close();
        }
        encoder.close();
        if (denoiser != null) {
            denoiser.close();
        }
        flush();
    }

    private final AtomicLong sequenceNumber = new AtomicLong();
    private volatile boolean stopPacketSent = true;

    private void sendAudioPacket(short[] audio, boolean whispering) {
        try {
            if (connection != null && connection.isInitialized()) {
                byte[] encoded = encoder.encode(audio);
                connection.sendToServer(new NetworkMessage(new MicPacket(encoded, whispering, sequenceNumber.getAndIncrement())));
                stopPacketSent = false;
            }
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to send audio packet", e);
        }
        try {
            if (client != null && client.getRecorder() != null) {
                client.getRecorder().appendChunk(Minecraft.getMinecraft().getSession().getProfile().getId(), System.currentTimeMillis(), PositionalAudioUtils.convertToStereo(audio));
            }
        } catch (IOException e) {
            Voicechat.LOGGER.error("Failed to record audio", e);
            client.setRecording(false);
        }
    }

    private void sendStopPacket() {
        if (stopPacketSent) {
            return;
        }

        if (connection == null || !connection.isInitialized()) {
            return;
        }
        try {
            connection.sendToServer(new NetworkMessage(new MicPacket(new byte[0], false, sequenceNumber.getAndIncrement())));
            stopPacketSent = true;
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to send stop packet", e);
        }
    }
}
