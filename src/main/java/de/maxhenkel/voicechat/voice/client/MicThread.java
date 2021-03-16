package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.MicPacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.OpusEncoder;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class MicThread extends Thread {

    private Client client;
    private TargetDataLine mic;
    private boolean running;
    private boolean microphoneLocked;
    private OpusEncoder encoder;

    public MicThread(Client client) throws LineUnavailableException {
        this.client = client;
        this.running = true;
        this.encoder = new OpusEncoder(AudioChannelConfig.getSampleRate(), AudioChannelConfig.getFrameSize(), Main.SERVER_CONFIG.voiceChatMtuSize.get(), Main.SERVER_CONFIG.voiceChatCodec.get().getOpusValue());
        setDaemon(true);
        setName("MicrophoneThread");
        AudioFormat af = AudioChannelConfig.getMonoFormat();
        mic = DataLines.getMicrophone();
        mic.open(af);
    }

    @Override
    public void run() {
        while (running) {
            // Checking here for timeouts, because we don't have any other looping thread
            client.checkTimeout();
            if (microphoneLocked) {
                Utils.sleep(10);
            } else {
                MicrophoneActivationType type = Main.CLIENT_CONFIG.microphoneActivationType.get();
                if (Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().isDisabled()) {
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

        if (Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().isMuted()) {
            activating = false;
            if (mic.isActive()) {
                mic.stop();
                mic.flush();
            }

            return;
        }

        int dataLength = AudioChannelConfig.getFrameSize();

        mic.start();

        if (mic.available() < dataLength) {
            Utils.sleep(1);
            return;
        }
        byte[] buff = new byte[dataLength];
        mic.read(buff, 0, buff.length);
        Utils.adjustVolumeMono(buff, Main.CLIENT_CONFIG.microphoneAmplification.get().floatValue());

        int offset = Utils.getActivationOffset(buff, Main.CLIENT_CONFIG.voiceActivationThreshold.get());
        if (activating) {
            if (offset < 0) {
                if (deactivationDelay >= Main.CLIENT_CONFIG.deactivationDelay.get()) {
                    activating = false;
                    deactivationDelay = 0;
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
        int dataLength = AudioChannelConfig.getFrameSize();
        if (!Main.CLIENT_VOICE_EVENTS.getPttKeyHandler().isPTTDown()) {
            if (wasPTT) {
                mic.stop();
                mic.flush();
                wasPTT = false;
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
        Utils.adjustVolumeMono(buff, Main.CLIENT_CONFIG.microphoneAmplification.get().floatValue());
        sendAudioPacket(buff);
    }

    private long sequenceNumber = 0L;

    private void sendAudioPacket(byte[] data) {
        try {
            byte[] encoded = encoder.encode(data);
            client.sendToServer(new NetworkMessage(new MicPacket(encoded, sequenceNumber++), client.getSecret()));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    }
}
