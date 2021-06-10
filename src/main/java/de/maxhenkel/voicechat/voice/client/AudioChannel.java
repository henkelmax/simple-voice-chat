package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.voice.common.OpusDecoder;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.Pair;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AudioChannel extends Thread {

    private Minecraft minecraft;
    private Client client;
    private UUID uuid;
    private BlockingQueue<SoundPacket> queue;
    private long lastPacketTime;
    private SourceDataLine speaker;
    private FloatControl gainControl;
    private boolean stopped;
    private OpusDecoder decoder;
    private long lastSequenceNumber;

    public AudioChannel(Client client, UUID uuid) {
        this.client = client;
        this.uuid = uuid;
        this.queue = new LinkedBlockingQueue<>();
        this.lastPacketTime = System.currentTimeMillis();
        this.stopped = false;
        this.decoder = new OpusDecoder(client.getAudioChannelConfig().getSampleRate(), client.getAudioChannelConfig().getFrameSize(), client.getMtuSize());
        this.lastSequenceNumber = -1L;
        this.minecraft = Minecraft.getInstance();
        setDaemon(true);
        setName("AudioChannelThread-" + uuid.toString());
        Voicechat.LOGGER.debug("Creating audio channel for " + uuid);
    }

    public boolean canKill() {
        return System.currentTimeMillis() - lastPacketTime > 30_000L;
    }

    public void closeAndKill() {
        Voicechat.LOGGER.debug("Closing audio channel for " + uuid);
        stopped = true;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void addToQueue(SoundPacket p) {
        queue.add(p);
    }

    @Override
    public void run() {
        try {
            AudioFormat af = client.getAudioChannelConfig().getStereoFormat();
            speaker = DataLines.getSpeaker();
            speaker.open(af);
            gainControl = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);
            while (!stopped) {

                if (VoicechatClient.CLIENT.getPlayerStateManager().isDisabled()) {
                    speaker.stop();
                    queue.clear();
                    closeAndKill();
                    return;
                }

                // Stopping the data line when the buffer is empty
                // to prevent the last sound getting repeated
                if (speaker.isActive() && speaker.getBufferSize() - speaker.available() <= 0) {
                    speaker.stop();
                    lastSequenceNumber = -1L;
                }

                // Flush the speaker if the buffer is too full to avoid too big delays
                if (speaker.isActive() && speaker.getBufferSize() - speaker.available() > client.getAudioChannelConfig().maxSpeakerBufferSize()) {
                    CooldownTimer.run("clear_audio_buffer", () -> {
                        Voicechat.LOGGER.warn("Clearing buffers to avoid audio delay");
                    });
                    speaker.stop();
                    speaker.flush();
                    lastSequenceNumber = -1L;
                }

                SoundPacket packet = queue.poll(10, TimeUnit.MILLISECONDS);
                if (packet == null) {
                    continue;
                }
                lastPacketTime = System.currentTimeMillis();

                if (lastSequenceNumber >= 0 && packet.getSequenceNumber() <= lastSequenceNumber) {
                    continue;
                }

                // Filling the speaker with silence for one packet size
                // to build a small buffer to compensate for network latency
                if (speaker.getBufferSize() - speaker.available() <= 0) {
                    byte[] data = new byte[Math.min(client.getAudioChannelConfig().getFrameSize() * VoicechatClient.CLIENT_CONFIG.outputBufferSize.get(), speaker.getBufferSize() - client.getAudioChannelConfig().getFrameSize())];
                    speaker.write(data, 0, data.length);
                }
                if (minecraft.level == null || minecraft.player == null) {
                    continue;
                }

                client.getTalkCache().updateTalking(uuid);

                if (lastSequenceNumber >= 0) {
                    int packetsToCompensate = (int) (packet.getSequenceNumber() - (lastSequenceNumber + 1));
                    for (int i = 0; i < packetsToCompensate; i++) {
                        if (speaker.available() < client.getAudioChannelConfig().getFrameSize()) {
                            Voicechat.LOGGER.debug("Could not compensate more than " + i + " audio packets");
                            break;
                        }
                        writeToSpeaker(decoder.decode(null));
                    }
                }

                lastSequenceNumber = packet.getSequenceNumber();

                byte[] decodedAudio = decoder.decode(packet.getData());

                writeToSpeaker(decodedAudio);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (speaker != null) {
                speaker.stop();
                speaker.flush();
                speaker.close();
            }
            decoder.close();
            Voicechat.LOGGER.debug("Closed audio channel for " + uuid);
        }
    }

    private void writeToSpeaker(byte[] monoData) {
        PlayerState state = VoicechatClient.CLIENT.getPlayerStateManager().getState(uuid);
        byte[] stereo;
        float percentage = 1F;

        if (state != null && state.hasGroup()) {
            stereo = Utils.convertToStereo(monoData, 1F, 1F);
        } else {
            Player player = minecraft.level.getPlayerByUUID(uuid);
            if (player == null) {
                return;
            }
            float distance = player.distanceTo(minecraft.player);
            float fadeDistance = (float) client.getVoiceChatFadeDistance();
            float maxDistance = (float) client.getVoiceChatDistance();

            if (distance > fadeDistance) {
                percentage = 1F - Math.min((distance - fadeDistance) / (maxDistance - fadeDistance), 1F);
            }

            if (VoicechatClient.CLIENT_CONFIG.stereo.get()) {
                Pair<Float, Float> stereoVolume = Utils.getStereoVolume(minecraft, player.position(), client.getVoiceChatDistance());
                stereo = Utils.convertToStereo(monoData, percentage * stereoVolume.getLeft(), percentage * stereoVolume.getRight());
            } else {
                stereo = Utils.convertToStereo(monoData, percentage, percentage);
            }
        }

        gainControl.setValue(Math.min(Math.max(Utils.percentageToDB(VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() * (float) VoicechatClient.VOLUME_CONFIG.getVolume(uuid)), gainControl.getMinimum()), gainControl.getMaximum()));

        speaker.write(stereo, 0, stereo.length);
        speaker.start();
    }

    public boolean isClosed() {
        return stopped;
    }

}