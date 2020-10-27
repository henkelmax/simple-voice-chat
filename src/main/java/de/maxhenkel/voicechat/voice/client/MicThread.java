package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.sound.sampled.*;
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
        AudioFormat af = AudioChannelConfig.getMonoFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);
        mic = (TargetDataLine) (AudioSystem.getLine(info));
        mic.open(af);
    }

    @Override
    public void run() {
        while (running) {
            if (microphoneLocked) {
                Utils.sleep(10);
            } else {
                MicrophoneActivationType type = Main.CLIENT_CONFIG.microphoneActivationType.get();
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
        int dataLength = AudioChannelConfig.getDataLength();

        mic.start();

        if (mic.available() < dataLength) {
            Utils.sleep(10);
            return;
        }
        byte[] buff = new byte[dataLength];
        while (mic.available() >= dataLength) {
            mic.read(buff, 0, buff.length);
        }
        Utils.adjustVolumeMono(buff, Main.CLIENT_CONFIG.microphoneAmplification.get().floatValue());

        int offset = Utils.getActivationOffset(buff, Main.CLIENT_CONFIG.voiceActivationThreshold.get());
        if (activating) {
            if (offset < 0) {
                if (deactivationDelay >= 2) {
                    activating = false;
                    sendStopPacket();
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
        int dataLength = AudioChannelConfig.getDataLength();
        if (!Main.KEY_PTT.isKeyDown()) {
            if (wasPTT) {
                mic.stop();
                mic.flush();
                sendStopPacket();
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
        Utils.adjustVolumeMono(buff, Main.CLIENT_CONFIG.microphoneAmplification.get().floatValue());
        sendAudioPacket(buff);
    }

    private void sendStopPacket() {
        try {
            // To prevent last sound repeating when no more audio data is available
            new NetworkMessage(new SoundPacket(new byte[0]), client.getPlayerUUID(), client.getSecret()).sendToServer(client.getSocket(), client.getAddress(), client.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAudioPacket(byte[] data) {
        try {
            new NetworkMessage(new SoundPacket(data), client.getPlayerUUID(), client.getSecret()).sendToServer(client.getSocket(), client.getAddress(), client.getPort());
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
