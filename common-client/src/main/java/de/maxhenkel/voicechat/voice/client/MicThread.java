package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
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
    private final Microphone mic;
    private boolean running;
    private boolean microphoneLocked;
    private final OpusEncoder encoder;
    private MicrophoneProcessor microphoneProcessor;

    public MicThread(@Nullable ClientVoicechat client, @Nullable ClientVoicechatConnection connection) throws MicrophoneException {
        this.client = client;
        this.connection = connection;
        this.running = true;
        this.encoder = OpusManager.createEncoder(connection == null ? ServerConfig.Codec.VOIP.getMode() : connection.getData().getCodec().getMode());
        microphoneProcessor = createMicrophoneProcessor();

        setDaemon(true);
        setName("MicrophoneThread");
        setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());
        mic = MicrophoneManager.createMicrophone();
    }

    private MicrophoneProcessor createMicrophoneProcessor() {
        MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
        if (MicrophoneActivationType.VOICE.equals(type)) {
            return new VoiceMicrophoneProcessor();
        } else {
            return new PTTMicrophoneProcessor();
        }
    }

    @Override
    public void run() {
        while (running) {
            MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
            if (!type.equals(microphoneProcessor.getActivationType())) {
                microphoneProcessor.close();
                microphoneProcessor = createMicrophoneProcessor();
            }

            if (connection != null) {
                // Checking here for timeouts, because we don't have any other looping thread
                connection.checkTimeout();
                if (!running) {
                    break;
                }
            }
            if (microphoneLocked || ClientManager.getPlayerStateManager().isDisabled()) {
                flushIfNeeded();

                if (!microphoneLocked && ClientManager.getPlayerStateManager().isDisabled()) {
                    microphoneProcessor.reset();
                    if (mic.isStarted()) {
                        mic.stop();
                    }
                }

                Utils.sleep(10);
                continue;
            }

            short[] processed = pollProcessedAudio(false);
            if (processed == null) {
                continue;
            }

            if (!microphoneProcessor.shouldTransmitAudio()) {
                processed = null;
            }

            sendAudio(processed, microphoneProcessor.isWhispering());
        }
    }

    @Nullable
    public short[] pollMic() {
        if (!mic.isStarted()) {
            mic.start();
        }
        if (mic.available() < AudioUtils.FRAME_SIZE) {
            Utils.sleep(5);
            return null;
        }
        return mic.read();
    }

    @Nullable
    public short[] pollProcessedAudio(boolean testing) {
        short[] audio = pollMic();
        if (audio == null) {
            return null;
        }
        microphoneProcessor.process(audio, testing);
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
        return !microphoneLocked && microphoneProcessor.shouldTransmitAudio();
    }

    public boolean isWhispering() {
        return microphoneProcessor.isWhispering();
    }

    public boolean shouldTransmitAudio() {
        return microphoneProcessor.shouldTransmitAudio();
    }

    public void setMicrophoneLocked(boolean microphoneLocked) {
        this.microphoneLocked = microphoneLocked;
        microphoneProcessor.reset();
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
        microphoneProcessor.close();
        flush();
    }

    public boolean isClosed() {
        return !running;
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
