package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.MicPacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.util.Arrays;

public class MicThread extends Thread {

    private Client client;
    private TargetDataLine mic;
    private boolean running;
    private boolean microphoneLocked;

    public MicThread(Client client) throws LineUnavailableException {
        this.client = client;
        this.running = true;
        setDaemon(true);
        setName("MicrophoneThread");
        AudioFormat af = client.getAudioChannelConfig().getMonoFormat();
        mic = DataLines.getMicrophone();
        mic.open(af);
    }

    @Override
    public void run() {
        while (running) {
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
    private byte[] lastBuff;

    private void voice() {
        wasPTT = false;

        if (client.isMuted()) {
            activating = false;
            if (mic.isActive()) {
                mic.stop();
                mic.flush();
            }

            return;
        }

        int dataLength = client.getAudioChannelConfig().getDataLength();

        mic.start();

        if (mic.available() < dataLength) {
            Utils.sleep(10);
            return;
        }
        byte[] buff = new byte[dataLength];
        while (mic.available() >= dataLength) {
            mic.read(buff, 0, buff.length);
        }
        Utils.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());

        int offset = Utils.getActivationOffset(buff, VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get());
        if (activating) {
            if (offset < 0) {
                if (deactivationDelay >= 2) {
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
            if (offset >= 0) {
                if (lastBuff != null) {
                    int lastPacketOffset = buff.length - offset;
                    sendAudioPacket(Arrays.copyOfRange(lastBuff, lastPacketOffset, lastBuff.length));
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
        int dataLength = client.getAudioChannelConfig().getDataLength();
        if (!VoicechatClient.KEY_PTT.isPressed()) {
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
            Utils.sleep(10);
            return;
        }
        byte[] buff = new byte[dataLength];
        while (mic.available() >= dataLength) { //TODO fix?
            mic.read(buff, 0, buff.length);
        }
        Utils.adjustVolumeMono(buff, VoicechatClient.CLIENT_CONFIG.microphoneAmplification.get().floatValue());
        sendAudioPacket(buff);
    }

    private void sendAudioPacket(byte[] data) {
        try {
            new NetworkMessage(new MicPacket(data), client.getSecret()).sendToServer(client);
        } catch (IOException e) {
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
    }
}
