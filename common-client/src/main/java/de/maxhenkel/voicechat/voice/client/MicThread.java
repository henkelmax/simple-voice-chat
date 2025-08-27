package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.plugins.ClientPluginManager;
import de.maxhenkel.voicechat.natives.OpusManager;
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
import java.util.function.Consumer;

public class MicThread extends Thread {

    @Nullable
    private final ClientVoicechat client;
    @Nullable
    private final ClientVoicechatConnection connection;
    @Nullable
    private Microphone mic;
    @Nullable
    private MicrophoneException microphoneError;
    private boolean running;
    private boolean microphoneLocked;
    private final OpusEncoder encoder;
    private MicrophoneProcessor microphoneProcessor;

    private final Consumer<MicrophoneException> onError;

    public MicThread(@Nullable ClientVoicechat client, @Nullable ClientVoicechatConnection connection, Consumer<MicrophoneException> onError) {
        this.client = client;
        this.connection = connection;
        this.onError = onError;
        this.running = true;
        this.encoder = OpusManager.createEncoder(connection == null ? ServerConfig.Codec.VOIP.getMode() : connection.getData().getCodec().getMode());
        microphoneProcessor = createMicrophoneProcessor();

        setDaemon(true);
        setName("MicrophoneThread");
        setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());
    }

    private MicrophoneProcessor createMicrophoneProcessor() {
        MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
        if (MicrophoneActivationType.VOICE.equals(type)) {
            return new VoiceMicrophoneProcessor();
        } else {
            return new PTTMicrophoneProcessor();
        }
    }

    public void getError(Consumer<MicrophoneException> onError) {
        if (microphoneError != null) {
            onError.accept(microphoneError);
        }
    }

    @Override
    public void run() {
        Microphone mic = getMic();
        if (mic == null) {
            return;
        }

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
        Microphone mic = getMic();
        if (mic == null) {
            throw new IllegalStateException("No microphone available");
        }
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

    @Nullable
    private Microphone getMic() {
        if (!running) {
            return null;
        }
        if (mic == null) {
            try {
                mic = MicrophoneManager.createMicrophone();
                Minecraft.getInstance().execute(ClientManager.instance()::checkMicrophonePermissions);
            } catch (MicrophoneException e) {
                onError.accept(e);
                microphoneError = e;
                running = false;
                return null;
            }
        }
        return mic;
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
        recorder.flushChunkThreaded(Minecraft.getInstance().getUser().getProfileId());
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
                client.getRecorder().appendChunk(Minecraft.getInstance().getUser().getProfileId(), System.currentTimeMillis(), PositionalAudioUtils.convertToStereo(audio));
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
