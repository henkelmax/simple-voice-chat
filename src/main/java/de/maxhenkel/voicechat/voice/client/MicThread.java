package de.maxhenkel.voicechat.voice.client;

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

    @Override
    public void run() {
        while (running) {
            if (!Main.KEY_PTT.isKeyDown()) {
                Utils.sleep(10);
                continue;
            }
            int dataLength = AudioChannelConfig.getDataLength();
            if (mic.available() < dataLength) {
                Utils.sleep(10);
                continue;
            }
            byte[] buff = new byte[dataLength];
            while (mic.available() >= dataLength) {
                mic.read(buff, 0, buff.length);
            }
            try {
                // TODO amplification

                (new NetworkMessage(new SoundPacket(buff))).send(toServer);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void close() {
        running = false;
        mic.close();
    }
}
