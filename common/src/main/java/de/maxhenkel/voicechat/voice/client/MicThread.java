package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.plugins.PluginManager;
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
    private final Microphone mic;
    private final VolumeManager volumeManager;
    private boolean running;
    private boolean microphoneLocked;
    private boolean wasWhispering;
    private final OpusEncoder encoder;
    @Nullable
    private Denoiser denoiser;

    public MicThread(@Nullable ClientVoicechat client, @Nullable ClientVoicechatConnection connection) throws MicrophoneException, NativeDependencyException {
        this.client = client;
        this.connection = connection;
        this.running = true;
        this.encoder = OpusEncoder.createEncoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, connection == null ? 1024 : connection.getData().getMtuSize(), connection == null ? ServerConfig.Codec.VOIP.getOpusValue() : connection.getData().getCodec().getOpusValue());
        if (encoder == null) {
            throw new NativeDependencyException("Failed to load Opus encoder");
        }
        encoder.open();

        this.denoiser = Denoiser.createDenoiser();
        if (denoiser == null) {
            Voicechat.LOGGER.warn("Denoiser not available");
        }
        volumeManager = new VolumeManager();

        setDaemon(true);
        setName("MicrophoneThread");

        mic = MicrophoneManager.getMicrophone();
    }

    @Override
    public void run() {
        while (running) {
            if (connection != null) {
                // Checking here for timeouts, because we don't have any other looping thread
                connection.checkTimeout();
            }
            if (microphoneLocked || ClientManager.getPlayerStateManager().isDisabled()) {
                activating = false;
                wasPTT = false;
                wasWhispering = false;
                flush();

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
            MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
            if (type.equals(MicrophoneActivationType.PTT)) {
                ptt(audio);
            } else if (type.equals(MicrophoneActivationType.VOICE)) {
                voice(audio);
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

    private void voice(short[] audio) {
        wasPTT = false;

        if (ClientManager.getPlayerStateManager().isMuted()) {
            activating = false;
            wasWhispering = false;
            flush();
            return;
        }

        wasWhispering = ClientManager.getPttKeyHandler().isWhisperDown();

        int offset = Utils.getActivationOffset(audio, VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get());
        if (activating) {
            if (offset < 0) {
                if (deactivationDelay >= VoicechatClient.CLIENT_CONFIG.deactivationDelay.get()) {
                    activating = false;
                    deactivationDelay = 0;
                    flush();
                } else {
                    sendAudioPacket(audio, wasWhispering);
                    deactivationDelay++;
                }
            } else {
                sendAudioPacket(audio, wasWhispering);
            }
        } else {
            if (offset > 0) {
                if (lastBuff != null) {
                    sendAudioPacket(lastBuff, wasWhispering);
                }
                sendAudioPacket(audio, wasWhispering);
                activating = true;
            }
        }
        lastBuff = audio;
    }

    private volatile boolean wasPTT;

    private void ptt(short[] audio) {
        activating = false;
        if (!ClientManager.getPttKeyHandler().isAnyDown()) {
            if (wasPTT) {
                wasPTT = false;
                wasWhispering = false;
                flush();
            }
            return;
        }
        wasPTT = true;
        wasWhispering = ClientManager.getPttKeyHandler().isWhisperDown();
        sendAudioPacket(audio, wasWhispering);
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
            encoder.resetState(); //TODO maybe this has a weird side effect when called every 10ms
        }
        if (client == null) {
            return;
        }
        AudioRecorder recorder = client.getRecorder();
        if (recorder == null) {
            return;
        }
        recorder.writeChunkThreaded(Minecraft.getInstance().getUser().getGameProfile().getId());
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

        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mic.stop();
        mic.close();
        encoder.close();
        if (denoiser != null) {
            denoiser.close();
        }
        flush();
    }

    private final AtomicLong sequenceNumber = new AtomicLong();
    private volatile boolean stopPacketSent = true;

    private void sendAudioPacket(short[] data, boolean whispering) {
        short[] audio = PluginManager.instance().onClientSound(data, whispering);
        if (audio == null) {
            return;
        }

        try {
            if (connection != null && connection.isAuthenticated()) {
                byte[] encoded = encoder.encode(audio);
                connection.sendToServer(new NetworkMessage(new MicPacket(encoded, whispering, sequenceNumber.getAndIncrement())));
                stopPacketSent = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (client != null && client.getRecorder() != null) {
                client.getRecorder().appendChunk(Minecraft.getInstance().getUser().getGameProfile(), System.currentTimeMillis(), Utils.convertToStereo(audio));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendStopPacket() {
        if (stopPacketSent) {
            return;
        }

        if (connection == null || !connection.isAuthenticated()) {
            return;
        }
        try {
            connection.sendToServer(new NetworkMessage(new MicPacket(new byte[0], false, sequenceNumber.getAndIncrement())));
            stopPacketSent = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
