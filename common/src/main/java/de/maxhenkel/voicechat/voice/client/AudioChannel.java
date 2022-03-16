package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.voice.client.speaker.Speaker;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerManager;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public class AudioChannel extends Thread {

    private final Minecraft minecraft;
    private final ClientVoicechat client;
    private final InitializationData initializationData;
    private final UUID uuid;
    private final BlockingQueue<SoundPacket<?>> queue;
    private final AudioPacketBuffer packetBuffer;
    private long lastPacketTime;
    private Speaker speaker;
    private boolean stopped;
    private final OpusDecoder decoder;
    private long lastSequenceNumber;

    public AudioChannel(ClientVoicechat client, InitializationData initializationData, UUID uuid) throws NativeDependencyException {
        this.client = client;
        this.initializationData = initializationData;
        this.uuid = uuid;
        this.queue = new LinkedBlockingQueue<>();
        this.packetBuffer = new AudioPacketBuffer(VoicechatClient.CLIENT_CONFIG.audioPacketThreshold.get());
        this.lastPacketTime = System.currentTimeMillis();
        this.stopped = false;
        this.decoder = OpusDecoder.createDecoder(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, initializationData.getMtuSize());
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

            speaker = SpeakerManager.createSpeaker(client.getSoundManager(), uuid);

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

                if (!packet.isFromClientAudioChannel() && lastSequenceNumber >= 0 && packet.getSequenceNumber() <= lastSequenceNumber) {
                    continue;
                }

                if (minecraft.level == null || minecraft.player == null) {
                    continue;
                }

                if (packet.getData().length == 0) {
                    if (packet instanceof PlayerSoundPacket) {
                        PlayerSoundPacket playerSoundPacket = (PlayerSoundPacket) packet;
                        PluginManager.instance().onReceiveEntityClientSound(uuid, new short[0], playerSoundPacket.isWhispering());
                    } else if (packet instanceof LocationSoundPacket) {
                        LocationSoundPacket locationSoundPacket = (LocationSoundPacket) packet;
                        PluginManager.instance().onReceiveLocationalClientSound(uuid, new short[0], locationSoundPacket.getLocation());
                    } else if (packet instanceof GroupSoundPacket) {
                        PluginManager.instance().onReceiveStaticClientSound(uuid, new short[0]);
                    }
                    lastSequenceNumber = -1L;
                    packetBuffer.clear();
                    flushRecording();
                    decoder.resetState();
                    continue;
                }

                if (!packet.isFromClientAudioChannel() && lastSequenceNumber >= 0) {
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

                short[] decodedAudio;
                if (packet.isFromClientAudioChannel()) {
                    decodedAudio = Utils.bytesToShorts(packet.getData());
                } else {
                    decodedAudio = decoder.decode(packet.getData());
                }

                writeToSpeaker(packet, decodedAudio);
            }
        } catch (InterruptedException ignored) {
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (speaker != null) {
                flushRecording();
                speaker.close();
            }
            decoder.close();
            Voicechat.LOGGER.info("Closed audio channel for " + uuid);
        }
    }

    private void flushRecording() {
        AudioRecorder recorder = client.getRecorder();
        if (recorder == null) {
            return;
        }
        recorder.flushChunkThreaded(uuid);
    }

    private void writeToSpeaker(Packet<?> packet, short[] monoData) {
        @Nullable PlayerEntity player = minecraft.level.getPlayerByUUID(uuid);

        float playerVolume;

        if (player != null) {
            playerVolume = (float) VoicechatClient.VOLUME_CONFIG.getVolume(uuid);
        } else {
            playerVolume = (float) VoicechatClient.VOLUME_CONFIG.getVolume(Util.NIL_UUID);
        }

        float volume = VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() * playerVolume;

        if (packet instanceof GroupSoundPacket) {
            short[] processedMonoData = PluginManager.instance().onReceiveStaticClientSound(uuid, monoData);
            speaker.play(processedMonoData, volume, null);
            client.getTalkCache().updateTalking(uuid, false);
            appendRecording(() -> PositionalAudioUtils.convertToStereo(processedMonoData));
        } else if (packet instanceof PlayerSoundPacket) {
            PlayerSoundPacket soundPacket = (PlayerSoundPacket) packet;
            if (player == null) {
                return;
            }
            if (player == minecraft.cameraEntity) {
                short[] processedMonoData = PluginManager.instance().onReceiveStaticClientSound(uuid, monoData);
                speaker.play(processedMonoData, volume, null);
                client.getTalkCache().updateTalking(uuid, soundPacket.isWhispering());
                appendRecording(() -> PositionalAudioUtils.convertToStereo(processedMonoData));
                return;
            }
            Vector3d pos = player.getEyePosition(1F);

            short[] processedMonoData = PluginManager.instance().onReceiveEntityClientSound(uuid, monoData, soundPacket.isWhispering());

            if (pos.distanceTo(minecraft.gameRenderer.getMainCamera().getPosition()) > initializationData.getVoiceChatDistance() + 1D) {
                return;
            }

            float crouchMultiplayer = player.isCrouching() ? (float) initializationData.getCrouchDistanceMultiplier() : 1F;
            float whisperMultiplayer = soundPacket.isWhispering() ? (float) initializationData.getWhisperDistanceMultiplier() : 1F;
            float multiplier = crouchMultiplayer * whisperMultiplayer;
            float outputVolume = volume * PositionalAudioUtils.getDistanceVolume(initializationData, pos, multiplier);
            speaker.play(processedMonoData, outputVolume, pos);
            if (outputVolume >= 0.01F) {
                client.getTalkCache().updateTalking(uuid, soundPacket.isWhispering());
            }
            appendRecording(() -> PositionalAudioUtils.convertToStereoForRecording(initializationData, pos, processedMonoData, multiplier));
        } else if (packet instanceof LocationSoundPacket) {
            LocationSoundPacket p = (LocationSoundPacket) packet;
            short[] processedMonoData = PluginManager.instance().onReceiveLocationalClientSound(uuid, monoData, p.getLocation());
            if (p.getLocation().distanceTo(minecraft.gameRenderer.getMainCamera().getPosition()) > initializationData.getVoiceChatDistance() + 1D) {
                return;
            }
            speaker.play(processedMonoData, volume * PositionalAudioUtils.getDistanceVolume(initializationData, p.getLocation()), p.getLocation());
            client.getTalkCache().updateTalking(uuid, false);
            appendRecording(() -> PositionalAudioUtils.convertToStereoForRecording(initializationData, p.getLocation(), processedMonoData));
        }
    }

    private void appendRecording(Supplier<short[]> stereo) {
        if (client.getRecorder() != null) {
            try {
                client.getRecorder().appendChunk(uuid, System.currentTimeMillis(), stereo.get());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isClosed() {
        return stopped;
    }

}