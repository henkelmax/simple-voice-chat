package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public class AudioChannel extends Thread {

    private final Minecraft minecraft;
    private final ClientVoicechat client;
    private final ClientVoicechatConnection clientConnection;
    private final UUID uuid;
    private final BlockingQueue<SoundPacket<?>> queue;
    private final AudioPacketBuffer packetBuffer;
    private long lastPacketTime;
    private ALSpeaker speaker;
    private boolean stopped;
    private final OpusDecoder decoder;
    private long lastSequenceNumber;

    public AudioChannel(ClientVoicechat client, ClientVoicechatConnection clientConnection, UUID uuid) throws NativeDependencyException {
        this.client = client;
        this.clientConnection = clientConnection;
        this.uuid = uuid;
        this.queue = new LinkedBlockingQueue<>();
        this.packetBuffer = new AudioPacketBuffer(VoicechatClient.CLIENT_CONFIG.audioPacketThreshold.get());
        this.lastPacketTime = System.currentTimeMillis();
        this.stopped = false;
        this.decoder = OpusDecoder.createDecoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, clientConnection.getData().getMtuSize());
        if (decoder == null) {
            throw new NativeDependencyException("Failed to load Opus decoder");
        }
        this.lastSequenceNumber = -1L;
        this.minecraft = Minecraft.getInstance();
        setDaemon(true);
        setName("AudioChannelThread-" + uuid.toString());
        Voicechat.LOGGER.info("Creating audio channel for " + uuid);
    }

    public boolean canKill() {
        return System.currentTimeMillis() - lastPacketTime > 30_000L;
    }

    public void closeAndKill() {
        Voicechat.LOGGER.info("Closing audio channel for " + uuid);
        stopped = true;
        queue.clear();
        if (Thread.currentThread() == this) {
            return;
        }
        interrupt();
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public void addToQueue(SoundPacket<?> p) {
        queue.add(p);
    }

    @Override
    public void run() {
        try {
            if (client.getSoundManager() == null) {
                throw new IllegalStateException("Started audio channel without sound manager");
            }
            speaker = ClientCompatibilityManager.INSTANCE.createSpeaker(client.getSoundManager(), SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE);
            speaker.open();
            while (!stopped) {

                if (ClientManager.getPlayerStateManager().isDisabled()) {
                    closeAndKill();
                    return;
                }

                SoundPacket<?> packet = packetBuffer.poll(queue);
                if (packet == null) {
                    continue;
                }
                lastPacketTime = System.currentTimeMillis();

                if (lastSequenceNumber >= 0 && packet.getSequenceNumber() <= lastSequenceNumber) {
                    continue;
                }

                if (minecraft.level == null || minecraft.player == null) {
                    continue;
                }

                speaker.checkBufferEmpty(this::flushRecordingSync);

                if (packet.getData().length == 0) {
                    lastSequenceNumber = -1L;
                    packetBuffer.clear();
                    speaker.runInContext(this::flushRecordingSync);
                    decoder.resetState();
                    continue;
                }

                if (lastSequenceNumber >= 0) {
                    int packetsToCompensate = (int) (packet.getSequenceNumber() - (lastSequenceNumber + 1));

                    if (packetsToCompensate > 0) {
                        Voicechat.LOGGER.debug("Compensating {}/{} packets ", packetsToCompensate >= 4 ? 0 : packetsToCompensate, packetsToCompensate);
                    }

                    if (packetsToCompensate <= 4) {
                        for (int i = 0; i < packetsToCompensate; i++) {
                            writeToSpeaker(packet, decoder.decode(null));
                        }
                    }
                }

                lastSequenceNumber = packet.getSequenceNumber();

                short[] decodedAudio = decoder.decode(packet.getData());

                writeToSpeaker(packet, decodedAudio);
            }
        } catch (InterruptedException ignored) {
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (speaker != null) {
                speaker.runInContext(this::flushRecordingSync);
                speaker.close();
            }
            decoder.close();
            Voicechat.LOGGER.info("Closed audio channel for " + uuid);
        }
    }

    private void flushRecordingSync() {
        AudioRecorder recorder = client.getRecorder();
        if (recorder == null) {
            return;
        }
        recorder.writeChunkThreaded(uuid);
    }

    private void writeToSpeaker(Packet<?> packet, short[] monoData) {
        @Nullable Player player = minecraft.level.getPlayerByUUID(uuid);

        float playerVolume;

        if (player != null) {
            playerVolume = (float) VoicechatClient.VOLUME_CONFIG.getVolume(uuid);
        } else {
            playerVolume = (float) VoicechatClient.VOLUME_CONFIG.getVolume(Util.NIL_UUID);
        }

        float volume = VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() * playerVolume;
        boolean stereo = VoicechatClient.CLIENT_CONFIG.stereo.get();

        if (packet instanceof GroupSoundPacket) {
            speaker.write(monoData, volume, null);
            client.getTalkCache().updateTalking(uuid, false);
            appendRecording(player, () -> Utils.convertToStereo(monoData, 1F, 1F));
        } else if (packet instanceof PlayerSoundPacket soundPacket) {
            if (player == null) {
                return;
            }
            if (player == minecraft.cameraEntity) {
                speaker.write(monoData, volume, null);
                client.getTalkCache().updateTalking(uuid, soundPacket.isWhispering());
                appendRecording(player, () -> Utils.convertToStereo(monoData, 1F, 1F));
                return;
            }
            Vec3 pos = player.getEyePosition();

            float crouchMultiplayer = player.isCrouching() ? (float) clientConnection.getData().getCrouchDistanceMultiplier() : 1F;
            float whisperMultiplayer = soundPacket.isWhispering() ? (float) clientConnection.getData().getWhisperDistanceMultiplier() : 1F;
            float multiplier = crouchMultiplayer * whisperMultiplayer;
            float outputVolume = volume * getDistanceVolume(pos, multiplier);
            speaker.write(monoData, outputVolume, stereo ? pos : null);
            if (outputVolume >= 0.01F) {
                client.getTalkCache().updateTalking(uuid, soundPacket.isWhispering());
            }
            appendRecording(player, () -> convertLocationalPacketToStereo(pos, monoData, multiplier));
        } else if (packet instanceof LocationSoundPacket p) {
            speaker.write(monoData, volume * getDistanceVolume(p.getLocation()), stereo ? p.getLocation() : null);
            client.getTalkCache().updateTalking(uuid, false);
            appendRecording(player, () -> convertLocationalPacketToStereo(p.getLocation(), monoData));
        }
    }

    private short[] convertLocationalPacketToStereo(Vec3 pos, short[] monoData) {
        return convertLocationalPacketToStereo(pos, monoData, 1F);
    }

    private short[] convertLocationalPacketToStereo(Vec3 pos, short[] monoData, float distanceMultiplier) {
        float distanceVolume = getDistanceVolume(pos, distanceMultiplier);
        if (VoicechatClient.CLIENT_CONFIG.stereo.get()) {
            Pair<Float, Float> stereoVolume = Utils.getStereoVolume(minecraft, pos, clientConnection.getData().getVoiceChatDistance() * distanceMultiplier);
            return Utils.convertToStereo(monoData, distanceVolume * stereoVolume.getLeft(), distanceVolume * stereoVolume.getRight());
        } else {
            return Utils.convertToStereo(monoData, distanceVolume, distanceVolume);
        }
    }

    private float getDistanceVolume(Vec3 pos) {
        return getDistanceVolume(pos, 1F);
    }

    private float getDistanceVolume(Vec3 pos, float distanceMultiplier) {
        float distance = (float) pos.distanceTo(minecraft.cameraEntity.getEyePosition());
        float fadeDistance = (float) clientConnection.getData().getVoiceChatFadeDistance() * distanceMultiplier;
        float maxDistance = (float) clientConnection.getData().getVoiceChatDistance() * distanceMultiplier;

        if (distance < fadeDistance) {
            return 1F;
        } else if (distance > maxDistance) {
            return 0F;
        } else {
            float percentage = (distance - fadeDistance) / (maxDistance - fadeDistance);
            return 1F / (1F + (float) Math.exp((percentage * 12F) - 6F));
        }
    }

    private void appendRecording(Player player, Supplier<short[]> stereo) {
        speaker.runInContext(() -> {
            if (client.getRecorder() != null) {
                try {
                    client.getRecorder().appendChunk(player != null ? player.getGameProfile() : null, System.currentTimeMillis(), stereo.get());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isClosed() {
        return stopped;
    }

}