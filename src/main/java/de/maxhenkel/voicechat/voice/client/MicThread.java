package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

public class MicThread extends Thread {

    public static final double AMPLIFICATION = 1D;

    private ObjectOutputStream toServer;
    private TargetDataLine mic;
    private boolean running;

    public MicThread(ObjectOutputStream toServer) throws LineUnavailableException {
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
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
                gzipOutputStream.write(buff);
                gzipOutputStream.flush();
                gzipOutputStream.close();
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();

                toServer.writeObject(new NetworkMessage<>(new SoundPacket(byteArrayOutputStream.toByteArray())));
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
