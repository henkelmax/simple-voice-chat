package de.maxhenkel.voicechat.voice.client;


import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.commons.lang3.tuple.Pair;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.UUID;

public class AudioChannel extends Thread {

    private MinecraftClient minecraft;
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
        this.minecraft = MinecraftClient.getInstance();
        setDaemon(true);
        setName("AudioChannelThread-" + uuid.toString());
        Voicechat.LOGGER.debug("Creating audio channel for " + uuid);
    }

    public boolean canKill() {
        return System.currentTimeMillis() - lastPacketTime > 30_000L;
    }

    public void closeAndKill() {
        Voicechat.LOGGER.debug("Closing audio channel for " + uuid);
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
            AudioFormat af = client.getAudioChannelConfig().getStereoFormat();
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
                NetworkMessage message = queue.get(0);
                queue.remove(message);
                if (!(message.getPacket() instanceof SoundPacket)) {
                    continue;
                }
                SoundPacket soundPacket = (SoundPacket) (message.getPacket());

                // Filling the speaker with silence for one packet size
                // to build a small buffer to compensate for network latency
                if (speaker.getBufferSize() - speaker.available() <= 0) {
                    byte[] data = new byte[Math.min(client.getAudioChannelConfig().getDataLength() * 2 * VoicechatClient.CLIENT_CONFIG.outputBufferSize.get(), speaker.getBufferSize() - client.getAudioChannelConfig().getDataLength())];
                    speaker.write(data, 0, data.length);
                }
                PlayerEntity player = minecraft.world.getPlayerByUuid(soundPacket.getSender());
                if (player != null) {
                    client.getTalkCache().updateTalking(player.getUuid());
                    float distance = player.distanceTo(minecraft.player);
                    float percentage = 1F;
                    float fadeDistance = (float) client.getVoiceChatFadeDistance();
                    float maxDistance = (float) client.getVoiceChatDistance();

                    if (distance > fadeDistance) {
                        percentage = 1F - Math.min((distance - fadeDistance) / (maxDistance - fadeDistance), 1F);
                    }

                    gainControl.setValue(Math.min(Math.max(Utils.percentageToDB(percentage * VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() * (float) VoicechatClient.VOLUME_CONFIG.getVolume(player)), gainControl.getMinimum()), gainControl.getMaximum()));

                    byte[] mono = soundPacket.getData();

                    Pair<Float, Float> stereoVolume = Utils.getStereoVolume(minecraft.player.getPos(), minecraft.player.yaw, player.getPos(), client.getVoiceChatDistance());

                    byte[] stereo = Utils.convertToStereo(mono, stereoVolume.getLeft(), stereoVolume.getRight());
                    speaker.write(stereo, 0, stereo.length);
                    speaker.start();
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

    public boolean isClosed() {
        return stopped;
    }

}
