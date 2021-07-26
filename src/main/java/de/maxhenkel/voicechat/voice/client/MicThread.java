package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.MicPacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.OpusEncoder;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;

public class MicThread extends Thread {

    private Client client;
    private TargetDataLine mic;
    private boolean running;
    private boolean microphoneLocked;
    private OpusEncoder encoder;

    public MicThread(Client client) throws LineUnavailableException {
        this.client = client;
        this.running = true;
        this.encoder = new OpusEncoder(client.getAudioChannelConfig().getSampleRate(), client.getAudioChannelConfig().getFrameSize(), client.getMtuSize(), client.getCodec().getOpusValue());
        setDaemon(true);
        setName("MicrophoneThread");
        AudioFormat af = client.getAudioChannelConfig().getMonoFormat();
        mic = DataLines.getMicrophone(af);
        if (mic == null) {
            throw new LineUnavailableException("Could not find any microphone with the specified audio format");
        }
        mic.open(af);

        // This fixes the accumulating audio issue on some Linux systems
        mic.start();
        mic.stop();
        mic.flush();
    }

    @Override
    public void run() {
        while (running && client.isConnected()) {
            // Checking here for timeouts, because we don't have any other looping thread
            client.checkTimeout();
            if (microphoneLocked) {
                Utils.sleep(10);
            } else {
                MicrophoneActivationType type = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get();
                if (VoicechatClient.CLIENT.getPlayerStateManager().isDisabled()) {
                    Utils.sleep(10);
                } else if (type.equals(MicrophoneActivationType.PTT)) {
                    ptt();
                } else if (type.equals(MicrophoneActivationType.VOICE)) {
                    voice();
                }
            }
        }
    }

    private boolean activating;
    private int deactivationDelay;
    private byte[] lastBuff;

    private void voice() {
        wasPTT = false;

        if (VoicechatClient.CLIENT.getPlayerStateManager().isMuted()) {
            activating = false;
            if (mic.isActive()) {
                mic.stop();
                mic.flush();
            }
            flushRecording();
            Utils.sleep(10);
            return;
        }

        int dataLength = client.getAudioChannelConfig().getFrameSize();

        mic.start();

        if (mic.available() < dataLength) {
            Utils.sleep(1);
            return;
        }
        byte[] buff = new byte[dataLength];
        mic.read(buff, 0, buff.length);
        Utils.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());

        int offset = Utils.getActivationOffset(buff, VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get());
        if (activating) {
            if (offset < 0) {
                if (deactivationDelay >= VoicechatClient.CLIENT_CONFIG.deactivationDelay.get()) {
                    activating = false;
                    deactivationDelay = 0;
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
        int dataLength = client.getAudioChannelConfig().getFrameSize();

        if (!VoicechatClient.CLIENT.getPttKeyHandler().isPTTDown()) {
            if (wasPTT) {
                mic.stop();
                mic.flush();
                wasPTT = false;
                flushRecording();
            }
            Utils.sleep(10);
            return;
        } else {
            wasPTT = true;
        }

        mic.start();

        if (mic.available() < dataLength) {
            Utils.sleep(1);
            return;
        }
        byte[] buff = new byte[dataLength];
        mic.read(buff, 0, buff.length);
        Utils.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());
        sendAudioPacket(buff);
    }

    private long sequenceNumber = 0L;

    private void sendAudioPacket(byte[] data) {
        try {
            byte[] encoded = encoder.encode(data);
            client.sendToServer(new NetworkMessage(new MicPacket(encoded, sequenceNumber++)));
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

    private void flushRecording() {
        AudioRecorder recorder = client.getRecorder();
        if (recorder == null) {
            return;
        }
        recorder.writeChunkThreaded(Minecraft.getInstance().getUser().getGameProfile().getId());
    }

    public TargetDataLine getMic() {
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

    public void close() {
        running = false;
        mic.stop();
        mic.flush();
        mic.close();
        encoder.close();
        flushRecording();
    }

}
