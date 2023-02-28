package de.maxhenkel.voicechat.voice.client;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.lame4j.ShortArrayBuffer;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.plugins.impl.mp3.Mp3EncoderImpl;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioRecorder {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");

    private static final int MP3_BITRATE = 320;

    private final long timestamp;
    private final Path location;

    private final GameProfile ownProfile;
    private final Map<UUID, AudioChunk> chunks;
    private final Map<UUID, EncoderData> encoders;

    private final AudioFormat stereoFormat;

    private final ExecutorService threadPool;

    public AudioRecorder(Path location, long timestamp) {
        this.timestamp = timestamp;
        this.location = location;
        location.toFile().mkdirs();
        chunks = new ConcurrentHashMap<>();
        encoders = new ConcurrentHashMap<>();
        ownProfile = Minecraft.getInstance().getUser().getGameProfile();

        stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SoundManager.SAMPLE_RATE, 16, 2, 4, SoundManager.SAMPLE_RATE, false);

        threadPool = Executors.newSingleThreadExecutor();
    }

    public static AudioRecorder create() {
        long timestamp = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String recordingDestination = VoicechatClient.CLIENT_CONFIG.recordingDestination.get();
        Path location;
        if (recordingDestination.trim().isEmpty()) {
            location = CommonCompatibilityManager.INSTANCE.getGameDirectory().resolve("voicechat_recordings").resolve(FORMAT.format(cal.getTime()));
        } else {
            location = Paths.get(recordingDestination).resolve(FORMAT.format(cal.getTime()));
        }
        return new AudioRecorder(location, timestamp);
    }

    public Path getLocation() {
        return location;
    }

    public long getStartTime() {
        return timestamp;
    }

    public int getRecordedPlayerCount() {
        return encoders.size();
    }

    public String getDuration() {
        return getDuration(System.currentTimeMillis());
    }

    public String getDuration(long currentTime) {
        long duration = currentTime - timestamp;
        DateFormat fmt = new SimpleDateFormat(":mm:ss");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return (duration / (1000L * 60L * 60L)) + fmt.format(new Date(duration));
    }

    public String getStorage() {
        return getStorage(System.currentTimeMillis());
    }

    public String getStorage(long currentTime) {
        long durationSeconds = (currentTime - timestamp) / 1000L;
        long size = durationSeconds * MP3_BITRATE * 1000L / 8L * getRecordedPlayerCount();
        return FileUtils.byteCountToDisplaySize(size);
    }

    private String lookupName(UUID uuid) {
        if (uuid.equals(ownProfile.getId())) {
            return ownProfile.getName();
        }
        String username = VoicechatClient.USERNAME_CACHE.getUsername(uuid);
        if (username == null) {
            return "system-" + uuid;
        }
        return username;
    }

    public void appendChunk(UUID uuid, long chunkTimestamp, short[] data) throws IOException {
        if (data.length <= 0) {
            flushChunkThreaded(uuid);
            return;
        }

        if (!encoders.containsKey(uuid)) {
            Mp3Encoder encoder = Mp3EncoderImpl.createEncoder(stereoFormat, MP3_BITRATE, VoicechatClient.CLIENT_CONFIG.recordingQuality.get(), Files.newOutputStream(location.resolve(lookupName(uuid) + ".mp3"), StandardOpenOption.CREATE_NEW));
            encoders.put(uuid, new EncoderData(encoder, timestamp));
            if (encoder == null) {
                throw new IOException("Failed to load mp3 encoder");
            }
        }

        AudioChunk chunk = getChunk(uuid, chunkTimestamp);
        long passedTime = chunkTimestamp - chunk.endTimestamp;
        long threshold = VoicechatClient.CLIENT_CONFIG.outputBufferSize.get() * 20L;
        if (passedTime < threshold && chunk.getDuration() < 60_000) {
            chunk.add(data, chunkTimestamp);
        } else {
            flushChunkThreaded(uuid);
            chunk = getChunk(uuid, chunkTimestamp);
            chunk.add(data, chunkTimestamp);
        }
    }

    private void writeChunk(UUID playerUUID, AudioChunk chunk) throws IOException {
        EncoderData encoderData = encoders.get(playerUUID);

        if (encoderData == null) {
            Voicechat.LOGGER.error("Failed to find recording data for {}", playerUUID);
            return;
        }
        if (encoderData.encoder == null) {
            // This is already handled in appendChunk
            return;
        }

        long relativeTime = chunk.timestamp - encoderData.lastTimestamp;

        if (relativeTime < -100L) {
            Voicechat.LOGGER.warn("Audio snippet {} overlaps more than 100ms with previous snippet.", chunk.timestamp);
            return;
        } else if (relativeTime < -20L) {
            Voicechat.LOGGER.warn("Audio {} overlaps with previous snippet.", chunk.timestamp);
        }
        if (relativeTime < 0L) {
            relativeTime = 0L;
        }

        int silenceShorts = (int) (relativeTime * getSamplesPerMs() * stereoFormat.getChannels());
        int tenSeconds = (int) stereoFormat.getSampleRate() * stereoFormat.getChannels() * 10;
        int insertedSilence = 0;

        if (silenceShorts > tenSeconds) {
            short[] silence = new short[tenSeconds];
            while (insertedSilence + tenSeconds < silenceShorts) {
                encoderData.encoder.encode(silence);
                insertedSilence += tenSeconds;
            }
        }

        short[] silence = new short[silenceShorts - insertedSilence];
        encoderData.encoder.encode(silence);
        short[] audio = chunk.getData();
        encoderData.encoder.encode(audio);

        encoderData.lastTimestamp = chunk.timestamp + getAudioTimeMillis(audio.length);
    }

    public void flushChunkThreaded(UUID playerUUID) {
        AudioChunk chunk = getAndRemoveChunk(playerUUID);
        if (chunk == null) {
            return;
        }
        threadPool.execute(() -> {
            try {
                writeChunk(playerUUID, chunk);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Nullable
    private AudioChunk getAndRemoveChunk(UUID playerUUID) {
        return chunks.remove(playerUUID);
    }

    private AudioChunk getChunk(UUID uuid, long timestamp) {
        if (!chunks.containsKey(uuid)) {
            AudioChunk chunk = new AudioChunk(timestamp);
            chunks.put(uuid, chunk);
            return chunk;
        } else {
            return chunks.get(uuid);
        }
    }

    /**
     * Writes every unfinished audio chunk to disk.
     * Not threaded.
     */
    private void flush() throws IOException {
        for (Map.Entry<UUID, AudioChunk> chunk : chunks.entrySet()) {
            writeChunk(chunk.getKey(), chunk.getValue());
        }
    }

    public void close() {
        if (threadPool.isShutdown()) {
            throw new IllegalStateException("Recorder already closed");
        }
        threadPool.shutdown();
    }

    public void saveAndClose() {
        save();
        close();
    }

    private void save() {
        threadPool.execute(() -> {
            send(new TranslationTextComponent("message.voicechat.processing_recording_session"));
            try {
                Exception error = null;
                sendProgress(0F);
                try {
                    flush();
                    sendProgress(0.5F);
                } catch (IOException e) {
                    error = e;
                }

                for (EncoderData encoderData : encoders.values()) {
                    if (encoderData.encoder != null) {
                        try {
                            encoderData.encoder.close();
                        } catch (IOException e) {
                            error = e;
                        }
                    } else {
                        error = new IOException("Failed to load mp3 encoder");
                    }
                }
                if (error != null) {
                    throw error;
                }
                sendProgress(1F);
                send(new TranslationTextComponent("message.voicechat.save_session",
                        new StringTextComponent(location.normalize().toString())
                                .withStyle(TextFormatting.GRAY)
                                .withStyle(style -> style
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("message.voicechat.open_folder")))
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, location.normalize().toString()))))
                );
            } catch (Exception e) {
                Voicechat.LOGGER.error("Failed to save recording session", e);
                send(new TranslationTextComponent("message.voicechat.save_session_failed", e.getMessage()));
            }
        });
    }

    private void sendProgress(float progress) {
        send(new TranslationTextComponent("message.voicechat.processing_progress",
                new StringTextComponent(String.valueOf((int) (progress * 100F)))
                        .withStyle(TextFormatting.GRAY))
        );
    }

    private void send(ITextComponent msg) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player != null && mc.level != null) {
            player.sendMessage(msg, Util.NIL_UUID);
        } else {
            Voicechat.LOGGER.info("{}", msg.getString());
        }
    }

    private int getAudioTimeMillis(int audioShortLength) {
        return (audioShortLength / stereoFormat.getChannels()) / getSamplesPerMs();
    }

    private int getSamplesPerMs() {
        return ((int) stereoFormat.getSampleRate() / 1000);
    }

    private class AudioChunk {
        private final long timestamp;
        private final ShortArrayBuffer buffer;
        private long endTimestamp;

        public AudioChunk(long timestamp) {
            this.timestamp = timestamp;
            this.endTimestamp = timestamp;
            this.buffer = new ShortArrayBuffer();
        }

        public void add(short[] data, long timestamp) throws IOException {
            buffer.writeShorts(data);
            endTimestamp = timestamp + getDuration(data.length);
        }

        private long getDuration(int length) {
            long l = length * 1000L / stereoFormat.getChannels();
            return (long) ((double) l / stereoFormat.getSampleRate());
        }

        public short[] getData() {
            return buffer.toShortArray();
        }

        public long getDuration() {
            return endTimestamp - timestamp;
        }
    }

    private static class EncoderData {
        @Nullable
        private final Mp3Encoder encoder;
        private long lastTimestamp;

        public EncoderData(@Nullable Mp3Encoder encoder, long lastTimestamp) {
            this.encoder = encoder;
            this.lastTimestamp = lastTimestamp;
        }
    }

}
