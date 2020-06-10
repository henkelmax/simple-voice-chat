package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Config;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class AudioChannel extends Thread {
    private Minecraft minecraft;
    private UUID uuid;
    private ArrayList<NetworkMessage<?>> queue;
    private long lastPacketTime;
    private SourceDataLine speaker;
    private FloatControl gainControl;
    private boolean stopped;

    public AudioChannel(UUID uuid) {
        this.uuid = uuid;
        this.queue = new ArrayList<>();
        this.lastPacketTime = System.nanoTime();
        this.stopped = false;
        this.minecraft = Minecraft.getInstance();
        Main.LOGGER.debug("Creating audio channel for " + uuid);
    }

    public boolean canKill() {
        return System.nanoTime() - lastPacketTime > 30_000_000_000L;
    }

    public void closeAndKill() {
        Main.LOGGER.debug("Closing audio channel for " + uuid);
        if (speaker != null) {
            speaker.close();
        }
        stopped = true;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void addToQueue(NetworkMessage<?> m) {
        queue.add(m);
    }

    @Override
    public void run() {
        try {
            AudioFormat af = SoundPacket.DEFAULT_FORMAT;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(af);
            speaker.start();
            gainControl = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);
            while (!stopped) {
                if (queue.isEmpty()) {
                    Utils.sleep(10);
                    continue;
                }
                lastPacketTime = System.nanoTime();
                NetworkMessage<?> message = queue.get(0);
                queue.remove(message);
                if (message.getData() instanceof SoundPacket) {
                    SoundPacket soundPacket = (SoundPacket) (message.getData());
                    GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(soundPacket.getData()));
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    while (true) {
                        int b = gzipInputStream.read();
                        if (b == -1) {
                            break;
                        } else {
                            byteArrayOutputStream.write((byte) b);
                        }
                    }
                    byte[] toPlay = byteArrayOutputStream.toByteArray();

                    PlayerEntity player = minecraft.world.getPlayerByUuid(message.getPlayerUUID());
                    if (player != null) {
                        float distance = player.getDistance(minecraft.player);
                        float percentage = 1F;
                        float fadeDistance = Config.SERVER.VOICE_CHAT_FADE_DISTANCE.get().floatValue();
                        float maxDistance = Config.SERVER.VOICE_CHAT_DISTANCE.get().floatValue();

                        if (distance > fadeDistance) {
                            percentage = 1F - Math.min((distance - fadeDistance) / (maxDistance - fadeDistance), 1F);
                        }

                        gainControl.setValue(Utils.percentageToDB(percentage * Config.CLIENT.VOICE_CHAT_VOLUME.get().floatValue()));
                        speaker.write(toPlay, 0, toPlay.length);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (speaker != null) {
                speaker.close();
            }
        }
    }
}
