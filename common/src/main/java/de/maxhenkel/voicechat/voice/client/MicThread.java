package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.config.ServerConfig;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.IOException;

public class MicThread extends Thread implements ALMicrophone.MicrophoneListener {

    private final ClientVoicechat client;
    @Nullable
    private final ClientVoicechatConnection connection;
    private final ALMicrophone mic;
    private final VolumeManager volumeManager;
    private boolean running;
    private boolean microphoneLocked;
    private final OpusEncoder encoder;
    @Nullable
    private final Denoiser denoiser;

    public MicThread(ClientVoicechat client, @Nullable ClientVoicechatConnection connection) throws MicrophoneException, NativeDependencyException {
        this.client = client;
        this.connection = connection;
        this.running = true;
        this.encoder = OpusEncoder.createEncoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, connection == null ? 1024 : connection.getData().getMtuSize(), connection == null ? ServerConfig.Codec.VOIP.getOpusValue() : connection.getData().getCodec().getOpusValue());
        if (encoder == null) {
            throw new NativeDependencyException("Failed to load Opus encoder");
        }

        this.denoiser = Denoiser.createDenoiser();
        if (denoiser == null) {
            Voicechat.LOGGER.warn("Denoiser not available");
        }
        volumeManager = new VolumeManager();

        setDaemon(true);
        setName("MicrophoneThread");
        mic = new ALMicrophone(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, VoicechatClient.CLIENT_CONFIG.microphone.get(), this);
        mic.open();
    }

    @Override
    public void run() {
        while (running) {
            if (connection != null) {
                // Checking here for timeouts, because we don't have any other looping thread
                connection.checkTimeout();
            }
            if (microphoneLocked) {
                Utils.sleep(10);
            } else {
                MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
                if (type.equals(MicrophoneActivationType.PTT)) {
                    ptt();
                } else if (type.equals(MicrophoneActivationType.VOICE)) {
                    voice();
                }
            }
        }
    }

    private boolean activating;
    private int deactivationDelay;
    private short[] lastBuff;

    private void voice() {
        wasPTT = false;

        if (ClientManager.getPlayerStateManager().isMuted() || ClientManager.getPlayerStateManager().isDisabled()) {
            activating = false;
            mic.stop();
            flushRecording();
            Utils.sleep(10);
            return;
        }

        mic.start();

        if (mic.available() < SoundManager.FRAME_SIZE) {
            Utils.sleep(1);
            return;
        }
        short[] buff = new short[SoundManager.FRAME_SIZE];
        mic.read(buff);
        volumeManager.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());
        buff = denoiseIfEnabled(buff);

        int offset = Utils.getActivationOffset(buff, VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get());
        if (activating) {
            if (offset < 0) {
                if (deactivationDelay >= VoicechatClient.CLIENT_CONFIG.deactivationDelay.get()) {
                    activating = false;
                    deactivationDelay = 0;
                    mic.stop();
                    flushRecording();
                } else {
                    sendAudioPacket(buff);
                    deactivationDelay++;
                }
            } else {
                sendAudioPacket(buff);
            }
        } else {
            if (offset > 0) {
                if (lastBuff != null) {
                    sendAudioPacket(lastBuff);
                }
                sendAudioPacket(buff);
                activating = true;
            }
        }
        lastBuff = buff;
    }

    private boolean wasPTT;

    private void ptt() {
        activating = false;
        if (!ClientManager.getPttKeyHandler().isPTTDown() || ClientManager.getPlayerStateManager().isDisabled()) {
            if (wasPTT) {
                mic.stop();
                wasPTT = false;
                flushRecording();
            }
            Utils.sleep(10);
            return;
        } else {
            wasPTT = true;
        }

        mic.start();

        if (mic.available() < SoundManager.FRAME_SIZE) {
            Utils.sleep(1);
            return;
        }
        short[] buff = new short[SoundManager.FRAME_SIZE];
        mic.read(buff);
        volumeManager.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());
        buff = denoiseIfEnabled(buff);
        sendAudioPacket(buff);
    }

    private long sequenceNumber = 0L;

    private void sendAudioPacket(short[] data) {
        try {
            if (connection != null && connection.isConnected()) {
                byte[] encoded = encoder.encode(data);
                connection.sendToServer(new NetworkMessage(new MicPacket(encoded, sequenceNumber++)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (client.getRecorder() != null) {
                client.getRecorder().appendChunk(Minecraft.getInstance().getUser().getGameProfile(), System.currentTimeMillis(), Utils.convertToStereo(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public short[] denoiseIfEnabled(short[] audio) {
        if (denoiser != null && VoicechatClient.CLIENT_CONFIG.denoiser.get()) {
            return denoiser.denoise(audio);
        }
        return audio;
    }

    private void flushRecording() {
        AudioRecorder recorder = client.getRecorder();
        if (recorder == null) {
            return;
        }
        recorder.writeChunkThreaded(Minecraft.getInstance().getUser().getGameProfile().getId());
    }

    public ALMicrophone getMic() {
        return mic;
    }

    public boolean isTalking() {
        return !microphoneLocked && (activating || wasPTT);
    }

    public void setMicrophoneLocked(boolean microphoneLocked) {
        this.microphoneLocked = microphoneLocked;
        activating = false;
        wasPTT = false;
        deactivationDelay = 0;
        lastBuff = null;
    }

    @Nullable
    public Denoiser getDenoiser() {
        return denoiser;
    }

    public VolumeManager getVolumeManager() {
        return volumeManager;
    }

    public void close() {
        running = false;
        mic.stop();
        mic.close();
        encoder.close();
        if (denoiser != null) {
            denoiser.close();
        }
        flushRecording();
    }

    @Override
    public void onStart() {
        encoder.open();
    }

    @Override
    public void onStop() {
        sendStopPacket();
        encoder.resetState();
    }

    private void sendStopPacket() {
        if (connection == null || !connection.isConnected()) {
            return;
        }
        try {
            connection.sendToServer(new NetworkMessage(new MicPacket(new byte[0], sequenceNumber++)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
