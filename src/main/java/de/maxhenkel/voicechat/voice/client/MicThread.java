package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.sound.sampled.*;
import java.io.DataOutputStream;
import java.io.IOException;

public class MicThread extends Thread {

    public static final double AMPLIFICATION = 1D;

    private DataOutputStream toServer;
    private TargetDataLine mic;
    private boolean running;

    public MicThread(DataOutputStream toServer) throws LineUnavailableException {
        this.toServer = toServer;
        this.running = true;
        setDaemon(true);
        AudioFormat af = SoundPacket.DEFAULT_FORMAT;
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
            if (mic.available() < SoundPacket.DEFAULT_DATA_LENGTH) {
                Utils.sleep(10);
                continue;
            }
            byte[] buff = new byte[SoundPacket.DEFAULT_DATA_LENGTH];
            while (mic.available() >= SoundPacket.DEFAULT_DATA_LENGTH) {
                mic.read(buff, 0, buff.length);
            }
            try {
                for (int i = 0; i < buff.length; i++) {
                    buff[i] *= AMPLIFICATION;
                }

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
