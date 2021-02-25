package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.commons.lang3.tuple.Pair;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AudioChannel extends Thread {

    private Minecraft minecraft;
    private Client client;
    private UUID uuid;
    private Queue<NetworkMessage> queue;
    private long lastPacketTime;
    private SourceDataLine speaker;
    private FloatControl gainControl;
    private boolean stopped;

    public AudioChannel(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
        this.queue = new ConcurrentLinkedQueue<>();
        this.lastPacketTime = System.currentTimeMillis();
        this.stopped = false;
        this.minecraft = Minecraft.getInstance();
        setDaemon(true);
        setName("AudioChannelThread-" + uuid.toString());
        Main.LOGGER.debug("Creating audio channel for " + uuid);
    }

    public boolean canKill() {
        return System.currentTimeMillis() - lastPacketTime > 30_000L;
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

    public void addToQueue(NetworkMessage m) {
        queue.add(m);
    }

    @Override
    public void run() {
        try {
            AudioFormat af = AudioChannelConfig.getStereoFormat();
            speaker = DataLines.getSpeaker();
            speaker.open(af);
            gainControl = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);
            while (!stopped) {
                if (queue.isEmpty()) {
                    // Stopping the data line when the buffer is empty
                    // to prevent the last sound getting repeated
                    if (speaker.getBufferSize() - speaker.available() <= 0 && speaker.isActive()) {
                        speaker.stop();
                    }
                    Utils.sleep(10);
                    continue;
                }
                lastPacketTime = System.currentTimeMillis();

                // Filling the speaker with silence for one packet size
                // to build a small buffer to compensate for network latency
                if (speaker.getBufferSize() - speaker.available() <= 0) {
                    byte[] data = new byte[Math.min(AudioChannelConfig.getDataLength() * 2 * Main.CLIENT_CONFIG.outputBufferSize.get(), speaker.getBufferSize() - AudioChannelConfig.getDataLength())];
                    speaker.write(data, 0, data.length);
                }
                if (minecraft.world == null || minecraft.player == null) {
                    continue;
                }

                PlayerEntity player = minecraft.world.getPlayerByUuid(uuid);
                if (player == null) {
                    continue;
                }

                client.getTalkCache().updateTalking(player.getUniqueID());
                float distance = player.getDistance(minecraft.player);
                float percentage = 1F;
                float fadeDistance = Main.SERVER_CONFIG.voiceChatFadeDistance.get().floatValue();
                float maxDistance = Main.SERVER_CONFIG.voiceChatDistance.get().floatValue();

                if (distance > fadeDistance) {
                    percentage = 1F - Math.min((distance - fadeDistance) / (maxDistance - fadeDistance), 1F);
                }

                gainControl.setValue(Math.min(Math.max(Utils.percentageToDB(percentage * Main.CLIENT_CONFIG.voiceChatVolume.get().floatValue() * (float) Main.VOLUME_CONFIG.getVolume(player)), gainControl.getMinimum()), gainControl.getMaximum()));

                byte[] mono = gatherPacketData();

                Pair<Float, Float> stereoVolume = Utils.getStereoVolume(minecraft.player.getPositionVec(), minecraft.player.rotationYaw, player.getPositionVec());

                byte[] stereo = Utils.convertToStereo(mono, stereoVolume.getLeft(), stereoVolume.getRight());
                speaker.write(stereo, 0, stereo.length);
                speaker.start();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (speaker != null) {
                speaker.stop();
                speaker.flush();
                speaker.close();
            }
        }
    }

    private byte[] gatherPacketData() {
        ByteArrayOutputStream s = new ByteArrayOutputStream(AudioChannelConfig.getDataLength() * 2);
        NetworkMessage message;
        while ((message = queue.poll()) != null) {
            if (!(message.getPacket() instanceof SoundPacket)) {
                continue;
            }
            SoundPacket soundPacket = (SoundPacket) (message.getPacket());
            try {
                s.write(soundPacket.getData());
            } catch (IOException e) {
                break;
            }
        }
        return s.toByteArray();
    }

    public boolean isClosed() {
        return stopped;
    }

}
