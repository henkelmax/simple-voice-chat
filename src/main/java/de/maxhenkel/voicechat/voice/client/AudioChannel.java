package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Config;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.commons.lang3.tuple.Pair;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.UUID;

public class AudioChannel extends Thread {

    private Minecraft minecraft;
    private Client client;
    private UUID uuid;
    private ArrayList<NetworkMessage> queue;
    private long lastPacketTime;
    private SourceDataLine speaker;
    private FloatControl gainControl;
    private boolean stopped;

    public AudioChannel(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
        this.queue = new ArrayList<>();
        this.lastPacketTime = System.currentTimeMillis();
        this.stopped = false;
        this.minecraft = Minecraft.getInstance();
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
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(af);
            gainControl = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);
            while (!stopped) {
                if (queue.isEmpty()) {
                    Utils.sleep(10);
                    continue;
                }
                lastPacketTime = System.currentTimeMillis();
                NetworkMessage message = queue.get(0);
                queue.remove(message);
                if (message.getPacket() instanceof SoundPacket) {
                    SoundPacket soundPacket = (SoundPacket) (message.getPacket());
                    if (soundPacket.getData().length == 0) {
                        System.out.println("STOP");
                        speaker.stop();
                        speaker.flush();
                        continue;
                    }
                    PlayerEntity player = minecraft.world.getPlayerByUuid(message.getPlayerUUID());
                    if (player != null) {
                        client.getTalkCache().updateTalking(player.getUniqueID());
                        float distance = player.getDistance(minecraft.player);
                        float percentage = 1F;
                        float fadeDistance = Config.SERVER.VOICE_CHAT_FADE_DISTANCE.get().floatValue();
                        float maxDistance = Config.SERVER.VOICE_CHAT_DISTANCE.get().floatValue();

                        if (distance > fadeDistance) {
                            percentage = 1F - Math.min((distance - fadeDistance) / (maxDistance - fadeDistance), 1F);
                        }

                        gainControl.setValue(Math.min(Math.max(Utils.percentageToDB(percentage * Config.CLIENT.VOICE_CHAT_VOLUME.get().floatValue()), gainControl.getMinimum()), gainControl.getMaximum()));

                        byte[] mono = soundPacket.getData();

                        Pair<Float, Float> stereoVolume = Utils.getStereoVolume(minecraft.player.getPositionVector(), minecraft.player.rotationYaw, player.getPositionVector());

                        byte[] stereo = Utils.convertToStereo(mono, stereoVolume.getLeft(), stereoVolume.getRight());
                        speaker.start();
                        speaker.write(stereo, 0, stereo.length);
                    }
                }
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

}
