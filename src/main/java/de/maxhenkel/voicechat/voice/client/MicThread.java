package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Config;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.sound.sampled.*;
import java.io.DataOutputStream;
import java.io.IOException;

public class MicThread extends Thread {

    private DataOutputStream toServer;
    private TargetDataLine mic;
    private boolean running;

    public MicThread(DataOutputStream toServer) throws LineUnavailableException {
        this.toServer = toServer;
        this.running = true;
        setDaemon(true);
        AudioFormat af = AudioChannelConfig.getMonoFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, null);
        mic = (TargetDataLine) (AudioSystem.getLine(info));
        mic.open(af);
        mic.start();
    }

    private boolean wasPTT;

    @Override
    public void run() {
        while (running) {
            int dataLength = AudioChannelConfig.getDataLength();
            if (!Main.KEY_PTT.isKeyDown()) {
                if (wasPTT) {
                    try {
                        // To prevent last sound repeating when no more audio data is available
                        (new NetworkMessage(new SoundPacket(new byte[dataLength]))).send(toServer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    wasPTT = false;
                }
                Utils.sleep(10);
                continue;
            }
            if (mic.available() < dataLength) {
                Utils.sleep(10);
                continue;
            }
            byte[] buff = new byte[dataLength];
            while (mic.available() >= dataLength) {
                mic.read(buff, 0, buff.length);
            }
            Utils.adjustVolumeMono(buff, Config.CLIENT.MICROPHONE_AMPLIFICATION.get().floatValue());
            try {
                (new NetworkMessage(new SoundPacket(buff))).send(toServer);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            wasPTT = true;
        }
    }

    public TargetDataLine getMic() {
        return mic;
    }

    public void close() {
        running = false;
        mic.close();
    }
}
