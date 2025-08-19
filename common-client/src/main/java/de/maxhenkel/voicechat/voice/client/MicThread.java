package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusManager;
import de.maxhenkel.voicechat.voice.client.microphone.Microphone;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import de.maxhenkel.voicechat.voice.common.AudioUtils;
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
    @Nullable
    private AutomaticGainControl agc;

    public MicThread(@Nullable ClientVoicechat client, @Nullable ClientVoicechatConnection connection) throws MicrophoneException {
        this.client = client;
        this.connection = connection;
        this.running = true;
        this.encoder = OpusManager.createEncoder(connection == null ? ServerConfig.Codec.VOIP.getMode() : connection.getData().getCodec().getMode());

        this.denoiser = Denoiser.createDenoiser();
        if (denoiser == null) {
            Voicechat.LOGGER.warn("Denoiser not available");
        }
        this.agc = AutomaticGainControl.createAgc();
        if (agc == null) {
            Voicechat.LOGGER.warn("AGC not available");
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
                micActivator.stopActivating();
                resetPtt();
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
        if (agc != null && agc.isClosed()) {
            agc = AutomaticGainControl.createAgc();
        }

        if (mic.available() < AudioUtils.FRAME_SIZE) {
            Utils.sleep(5);
            return null;
        }
        short[] buff = mic.read();
        processAudio(buff);
        return buff;
    }

    private final MicActivator micActivator = new MicActivator();

    private boolean voice(short[] audio) {
        resetPtt();

        if (ClientManager.getPlayerStateManager().isMuted()) {
            micActivator.stopActivating();
            wasWhispering = false;
            return false;
        }

        wasWhispering = ClientManager.getPttKeyHandler().isWhisperDown();

        return micActivator.push(audio, a -> sendAudio(a, wasWhispering));
    }

    private boolean wasPTT;
    private boolean wasPTTActivating;
    private int pttDeactivation;

    private boolean ptt(short[] audio) {
        micActivator.stopActivating();

        if (!ClientManager.getPttKeyHandler().isAnyDown()) {
            if (wasPTT) {
                pttDeactivation = VoicechatClient.CLIENT_CONFIG.pttDeactivationDelay.get();
            }
            if (pttDeactivation <= 0) {
                if (wasPTTActivating) {
                    wasPTTActivating = false;
                    wasWhispering = false;
                }
                return false;
            } else {
                pttDeactivation--;
            }
            wasPTT = false;
        } else {
            wasPTT = true;
        }
        wasPTTActivating = true;

        wasWhispering = ClientManager.getPttKeyHandler().isWhisperDown();
        sendAudio(audio, wasWhispering);
        return true;
    }

    private void resetPtt() {
        wasPTT = false;
        wasPTTActivating = false;
        pttDeactivation = 0;
    }

    public void processAudio(short[] audio) {
        if (agc != null && VoicechatClient.CLIENT_CONFIG.agc.get()) {
            float speechProbability = 1F;

            if (denoiser != null) {
                if (VoicechatClient.CLIENT_CONFIG.denoiser.get()) {
                    speechProbability = denoiser.denoiseInPlace(audio);
                } else {
                    speechProbability = denoiser.getSpeechProbability(audio);
                }
            }

            agc.agc(audio);

            if (speechProbability >= 0.95F && shouldAdjustGain()) {
                agc.setIncrement(12);
            } else {
                agc.setIncrement(0);
            }
        } else {
            if (denoiser != null && VoicechatClient.CLIENT_CONFIG.denoiser.get()) {
                denoiser.denoiseInPlace(audio);
            }
            volumeManager.adjustVolume(audio, VoicechatClient.CLIENT_CONFIG.microphoneGain.get());
        }
    }

    public boolean shouldAdjustGain() {
        MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
        if (type.equals(MicrophoneActivationType.PTT)) {
            return wasPTTActivating;
        } else if (type.equals(MicrophoneActivationType.VOICE)) {
            return !ClientManager.getPlayerStateManager().isMuted();
        }
        return true;
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
        @Nullable short[] mergedAudio = ClientPluginManager.instance().onMergeClientSound(rawAudio);
        if (mergedAudio == null) {
            flushIfNeeded();
            return;
        }
        short[] finalAudio = ClientPluginManager.instance().onClientSound(mergedAudio, whispering);
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
        return !microphoneLocked && (micActivator.isActivating() || wasPTTActivating);
    }

    public boolean isWhispering() {
        return isTalking() && wasWhispering;
    }

    public void setMicrophoneLocked(boolean microphoneLocked) {
        this.microphoneLocked = microphoneLocked;
        micActivator.stopActivating();
        resetPtt();
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
        if (agc != null) {
            agc.close();
        }
        flush();
    }

    private final AtomicLong sequenceNumber = new AtomicLong();
    private volatile boolean stopPacketSent = true;

    private void sendAudioPacket(short[] audio, boolean whispering) {
        if (connection != null && connection.isInitialized()) {
            byte[] encoded = encoder.encode(audio);
            connection.sendToServer(new NetworkMessage(new MicPacket(encoded, whispering, sequenceNumber.getAndIncrement())));
            stopPacketSent = false;
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
        connection.sendToServer(new NetworkMessage(new MicPacket(new byte[0], false, sequenceNumber.getAndIncrement())));
        stopPacketSent = true;
    }
}
