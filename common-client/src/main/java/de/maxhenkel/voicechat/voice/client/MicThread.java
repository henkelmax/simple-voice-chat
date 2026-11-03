package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.debug.CooldownTimer;
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

    /**
     * To avoid duplicate mic usages, since some setups only allow one microphone
     */
    private static final Object MICROPHONE_LOCK = new Object();

    @Nullable
    private final ClientVoicechat client;
    @Nullable
    private final ClientVoicechatConnection connection;
    @Nullable
    private Microphone mic;
    @Nullable
    private MicrophoneException microphoneError;
    private volatile boolean running;
    private volatile boolean microphoneLocked;
    private final OpusEncoder encoder;
    private volatile MicrophoneProcessor microphoneProcessor;

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
        synchronized (MICROPHONE_LOCK) {
            try {
                processMicrophone();
            } finally {
                closeResources();
            }
        }
    }

    private void processMicrophone() {
        Microphone mic = getMic();
        if (mic == null) {
            return;
        }

        while (running) {
            checkMicConnection(mic);

            if (microphoneLocked || ClientManager.getPlayerStateManager().isDisabled()) {
                flushIfNeeded();

                if (!microphoneLocked && ClientManager.getPlayerStateManager().isDisabled()) {
                    microphoneProcessor.reset();
                    stopMic(mic);
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
        short[] audio = readMic();
        if (audio == null) {
            Utils.sleep(5);
        }
        return audio;
    }

    @Nullable
    private synchronized short[] readMic() {
        if (mic == null || !mic.isOpen()) {
            return null;
        }
        if (!mic.isStarted()) {
            mic.start();
        }
        if (mic.available() < AudioUtils.FRAME_SIZE) {
            return null;
        }
        return mic.read();
    }

    private synchronized void checkMicConnection(Microphone mic) {
        if (mic.isOpen() && mic.isDisconnected()) {
            Voicechat.LOGGER.warn("Microphone was disconnected");
            mic.close();
            microphoneProcessor.reset();
            flushIfNeeded();
            onError.accept(new MicrophoneException("Microphone was disconnected"));
        } else if (!mic.isOpen()) {
            CooldownTimer.run("microphone_reconnect", 1000L, () -> reopenMic(mic));
        }
    }

    private void reopenMic(Microphone mic) {
        try {
            mic.open();
            Voicechat.LOGGER.info("Microphone was reconnected");
        } catch (MicrophoneException e) {
            Voicechat.LOGGER.debug("Failed to reopen microphone: {}", e.getMessage());
        }
    }

    private synchronized void stopMic(Microphone mic) {
        if (mic.isStarted()) {
            mic.stop();
        }
    }

    @Nullable
    public short[] pollProcessedAudio(boolean testing) {
        short[] audio = pollMic();
        if (audio == null) {
            return null;
        }
        // Always do this here to ensure the thread that processes audio can do it
        MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
        if (!type.equals(microphoneProcessor.getActivationType())) {
            microphoneProcessor.close();
            microphoneProcessor = createMicrophoneProcessor();
        }
        microphoneProcessor.process(audio, testing);
        return audio;
    }

    @Nullable
    private synchronized Microphone getMic() {
        if (!running) {
            return null;
        }
        if (mic == null) {
            try {
                mic = MicrophoneManager.createMicrophone();
                Minecraft.getInstance().execute(ClientManager.instance()::checkMicrophonePermissions);
            } catch (MicrophoneException e) {
                stopWithError(e);
                return null;
            }
        }
        return mic;
    }

    private void stopWithError(MicrophoneException e) {
        onError.accept(e);
        microphoneError = e;
        running = false;
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
        return running && !microphoneLocked && microphoneProcessor.shouldTransmitAudio();
    }

    public boolean isWhispering() {
        return running && microphoneProcessor.isWhispering();
    }

    public boolean shouldTransmitAudio() {
        return microphoneProcessor.shouldTransmitAudio();
    }

    public void setMicrophoneLocked(boolean microphoneLocked) {
        this.microphoneLocked = microphoneLocked;
        microphoneProcessor.reset();
    }

    /**
     * Stops the thread, which then closes the microphone by itself, so it isn't closed while still being in use.
     */
    public void close() {
        running = false;
    }

    private synchronized void closeResources() {
        if (mic != null) {
            mic.close();
            mic = null;
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
