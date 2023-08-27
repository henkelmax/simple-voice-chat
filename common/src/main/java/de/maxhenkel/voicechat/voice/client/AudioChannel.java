package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.debug.VoicechatUncaughtExceptionHandler;
import de.maxhenkel.voicechat.integration.freecam.FreecamUtil;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.opus.OpusManager;
import de.maxhenkel.voicechat.voice.client.speaker.Speaker;
import de.maxhenkel.voicechat.voice.client.speaker.SpeakerManager;
import de.maxhenkel.voicechat.voice.common.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
    private long lostPackets;

    public AudioChannel(ClientVoicechat client, InitializationData initializationData, UUID uuid) {
        this.client = client;
        this.initializationData = initializationData;
        this.uuid = uuid;
        this.queue = new LinkedBlockingQueue<>();
        this.packetBuffer = new AudioPacketBuffer(VoicechatClient.CLIENT_CONFIG.audioPacketThreshold.get());
        this.lastPacketTime = System.currentTimeMillis();
        this.stopped = false;
        this.decoder = OpusManager.createDecoder();
        this.lastSequenceNumber = -1L;
        this.minecraft = Minecraft.getInstance();
        setDaemon(true);
        setName("AudioChannelThread-" + uuid.toString());
        setUncaughtExceptionHandler(new VoicechatUncaughtExceptionHandler());
        Voicechat.LOGGER.info("Creating audio channel for {}", uuid);
    }

    public boolean canKill() {
        return System.currentTimeMillis() - lastPacketTime > 30_000L;
    }

    public void closeAndKill() {
        Voicechat.LOGGER.info("Closing audio channel for {}", uuid);
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
                    if (packet instanceof PlayerSoundPacket playerSoundPacket) {
                        PluginManager.instance().onReceiveEntityClientSound(uuid, new short[0], playerSoundPacket.isWhispering(), playerSoundPacket.getDistance());
                    } else if (packet instanceof LocationSoundPacket locationSoundPacket) {
                        PluginManager.instance().onReceiveLocationalClientSound(uuid, new short[0], locationSoundPacket.getLocation(), locationSoundPacket.getDistance());
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
                        Voicechat.logDebug("Compensating {}/{} packets ", packetsToCompensate >= 4 ? 0 : packetsToCompensate, packetsToCompensate);
                    }

                    if (packetsToCompensate <= 4) {
                        lostPackets += packetsToCompensate;
                        for (int i = 0; i < packetsToCompensate; i++) {
                            writeToSpeaker(packet, decoder.decode(null));
                        }
                    } else {
                        Voicechat.logDebug("Skipping compensation for {} packets", packetsToCompensate);
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
            Voicechat.LOGGER.info("Closed audio channel for {}", uuid);
        }
    }

    private void flushRecording() {
        AudioRecorder recorder = client.getRecorder();
        if (recorder == null) {
            return;
        }
        recorder.flushChunkThreaded(uuid);
    }

    private void writeToSpeaker(SoundPacket<?> packet, short[] monoData) {
        float channelVolume;

        if (VoicechatClient.USERNAME_CACHE.has(uuid)) {
            channelVolume = (float) VoicechatClient.VOLUME_CONFIG.getPlayerVolume(uuid);
        } else if (packet.getCategory() != null) {
            channelVolume = (float) VoicechatClient.VOLUME_CONFIG.getCategoryVolume(packet.getCategory());
        } else {
            channelVolume = (float) VoicechatClient.VOLUME_CONFIG.getPlayerVolume(Util.NIL_UUID);
        }

        float volume = VoicechatClient.CLIENT_CONFIG.voiceChatVolume.get().floatValue() * channelVolume;

        if (packet instanceof GroupSoundPacket) {
            short[] processedMonoData = PluginManager.instance().onReceiveStaticClientSound(uuid, monoData);
            speaker.play(processedMonoData, volume, packet.getCategory());
            client.getTalkCache().updateTalking(uuid, false);
            appendRecording(() -> PositionalAudioUtils.convertToStereo(processedMonoData));
        } else if (packet instanceof PlayerSoundPacket soundPacket) {
            @Nullable Entity entity = minecraft.level.getPlayerByUUID(uuid);
            if (entity == null) {
                Vec3 position = minecraft.gameRenderer.getMainCamera().getPosition();
                AABB box = new AABB(
                        position.x - soundPacket.getDistance() - 1F,
                        position.y - soundPacket.getDistance() - 1F,
                        position.z - soundPacket.getDistance() - 1F,
                        position.x + soundPacket.getDistance() + 1F,
                        position.y + soundPacket.getDistance() + 1F,
                        position.z + soundPacket.getDistance() + 1F
                );
                entity = minecraft.level.getEntities((Entity) null, box, e -> e.getUUID().equals(uuid)).stream().findAny().orElse(null);
                if (entity == null) {
                    return;
                }
            }
            if (entity == minecraft.cameraEntity) {
                short[] processedMonoData = PluginManager.instance().onReceiveStaticClientSound(uuid, monoData);
                speaker.play(processedMonoData, volume, soundPacket.getCategory());
                client.getTalkCache().updateTalking(uuid, soundPacket.isWhispering());
                appendRecording(() -> PositionalAudioUtils.convertToStereo(processedMonoData));
                return;
            }

            float deathVolume = 1F;
            if (entity instanceof LivingEntity) {
                deathVolume = Math.min(Math.max((20F - (float) ((LivingEntity) entity).deathTime) / 20F, 0F), 1F);
            }
            volume *= deathVolume;
            Vec3 pos = entity.getEyePosition();

            short[] processedMonoData = PluginManager.instance().onReceiveEntityClientSound(uuid, monoData, soundPacket.isWhispering(), soundPacket.getDistance());

            if (FreecamUtil.getDistanceTo(pos) > soundPacket.getDistance() + 1D) {
                return;
            }

            float distanceVolume = FreecamUtil.getDistanceVolume(soundPacket.getDistance(), pos);

            if (FreecamUtil.isFreecamEnabled()) {
                // Static, but with volume adjusted for distance
                volume *= distanceVolume;
                speaker.play(processedMonoData, volume, soundPacket.getCategory());
                if (distanceVolume > 0F) {
                    client.getTalkCache().updateTalking(uuid, soundPacket.isWhispering());
                }
                float recordingVolume = volume;
                appendRecording(() -> PositionalAudioUtils.convertToStereo(processedMonoData, recordingVolume));
                return;
            }

            speaker.play(processedMonoData, volume, pos, soundPacket.getCategory(), soundPacket.getDistance());
            if (distanceVolume > 0F) {
                client.getTalkCache().updateTalking(uuid, soundPacket.isWhispering());
            }
            float recordingVolume = deathVolume;
            appendRecording(() -> PositionalAudioUtils.convertToStereoForRecording(soundPacket.getDistance(), pos, processedMonoData, recordingVolume));
        } else if (packet instanceof LocationSoundPacket p) {
            short[] processedMonoData = PluginManager.instance().onReceiveLocationalClientSound(uuid, monoData, p.getLocation(), p.getDistance());
            if (FreecamUtil.getDistanceTo(p.getLocation()) > p.getDistance() + 1D) {
                return;
            }
            speaker.play(processedMonoData, volume, p.getLocation(), p.getCategory(), p.getDistance());
            client.getTalkCache().updateTalking(uuid, false);
            appendRecording(() -> PositionalAudioUtils.convertToStereoForRecording(p.getDistance(), p.getLocation(), processedMonoData));
        }
    }

    private void appendRecording(Supplier<short[]> stereo) {
        if (client.getRecorder() != null) {
            try {
                client.getRecorder().appendChunk(uuid, System.currentTimeMillis(), stereo.get());
            } catch (IOException e) {
                Voicechat.LOGGER.error("Failed to record audio", e);
                client.setRecording(false);
            }
        }
    }

    public boolean isClosed() {
        return stopped;
    }

    public BlockingQueue<SoundPacket<?>> getQueue() {
        return queue;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public AudioPacketBuffer getPacketBuffer() {
        return packetBuffer;
    }

    public long getLostPackets() {
        return lostPackets;
    }
}