package de.maxhenkel.voicechat.voice.client;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.plugins.impl.mp3.Mp3EncoderImpl;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.Tuple;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AudioRecorder {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");

    private final long timestamp;
    private final Path location;

    private final GameProfile ownProfile;
    private final Map<UUID, AudioChunk> chunks;
    private final Set<UUID> uniquePlayersRecorded;

    private final AudioFormat stereoFormat;

    private final ExecutorService threadPool;

    public AudioRecorder(Path location, long timestamp) {
        this.timestamp = timestamp;
        this.location = location;
        location.toFile().mkdirs();
        chunks = new ConcurrentHashMap<>();
        uniquePlayersRecorded = new HashSet<>();
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
        return uniquePlayersRecorded.size();
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
        return FileUtils.byteCountToDisplaySize((currentTime - timestamp) * (long) stereoFormat.getFrameSize() * ((long) stereoFormat.getFrameRate() / 1000L) * getRecordedPlayerCount());
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

    private Path getFilePath(UUID playerUUID, long timestamp) {
        return location.resolve(playerUUID.toString()).resolve(timestamp + ".wav");
    }

    public void appendChunk(UUID uuid, long timestamp, short[] data) throws IOException {
        if (data.length <= 0) {
            flushChunkThreaded(uuid);
            return;
        }
        uniquePlayersRecorded.add(uuid);
        AudioChunk chunk = getChunk(uuid, timestamp);
        long passedTime = timestamp - chunk.getEndTimestamp();
        long threshold = VoicechatClient.CLIENT_CONFIG.outputBufferSize.get() * 20L;
        if (passedTime < threshold) {
            chunk.add(data, timestamp);
        } else {
            flushChunkThreaded(uuid);
            chunk = getChunk(uuid, timestamp);
            chunk.add(data, timestamp);
        }
    }

    private void writeChunk(UUID playerUUID, AudioChunk chunk) throws IOException {
        File file = getFilePath(playerUUID, chunk.getTimestamp()).toFile();
        file.getParentFile().mkdirs();
        byte[] data = chunk.getBytes();
        writeWav(data, stereoFormat, file);
    }

    private static void writeWav(byte[] data, AudioFormat format, File file) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        AudioSystem.write(
                new AudioInputStream(
                        stream,
                        format,
                        data.length / format.getFrameSize()
                ),
                AudioFileFormat.Type.WAVE,
                file
        );
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
    public void flush() throws IOException {
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
            send(Component.translatable("message.voicechat.processing_recording_session"));
            try {
                AtomicLong time = new AtomicLong();
                convert(progress -> {
                    if (progress >= 1F || System.currentTimeMillis() - time.get() > 1000L) {
                        send(Component.translatable("message.voicechat.processing_progress",
                                Component.literal(String.valueOf((int) (progress * 100F)))
                                        .withStyle(ChatFormatting.GRAY))
                        );
                        time.set(System.currentTimeMillis());
                    }
                });
                send(Component.translatable("message.voicechat.save_session",
                        Component.literal(location.normalize().toString())
                                .withStyle(ChatFormatting.GRAY)
                                .withStyle(style -> style
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.voicechat.open_folder")))
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, location.normalize().toString()))))
                );
            } catch (Exception e) {
                e.printStackTrace();
                send(Component.translatable("message.voicechat.save_session_failed", e.getMessage()));
            }
        });
    }

    private void send(Component msg) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && mc.level != null) {
            player.sendSystemMessage(msg);
        } else {
            Voicechat.LOGGER.info(msg.getString());
        }
    }

    public void convert(Consumer<Float> progress) throws UnsupportedAudioFileException, IOException {
        flush();
        String[] directories = location.toFile().list();
        if (directories == null) {
            return;
        }
        for (int i = 0; i < directories.length; i++) {
            String directory = directories[i];
            float progressPerc = (float) i / (float) directories.length;
            UUID uuid;
            try {
                uuid = UUID.fromString(directory);
            } catch (Exception e) {
                return;
            }

            File userDir = location.resolve(directory).toFile();
            File[] files = userDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            Mp3Encoder encoder = Mp3EncoderImpl.createEncoder(stereoFormat, 320, 2, Files.newOutputStream(location.resolve(lookupName(uuid) + ".mp3"), StandardOpenOption.CREATE_NEW));
            if (encoder == null) {
                throw new IOException("Failed to load mp3 encoder");
            }
            convertFiles(files, encoder, p -> progress.accept(progressPerc + p * (1F / (float) directories.length)));
            encoder.close();
            try {
                FileUtils.deleteDirectory(userDir);
            } catch (IOException e) {
                Voicechat.LOGGER.warn("Failed to delete temporary recording data: {}", e.getMessage());
            }
        }
    }

    private boolean convertFiles(File[] files, Mp3Encoder encoder, Consumer<Float> progress) throws UnsupportedAudioFileException, IOException {
        boolean hasAudio = false;
        List<Tuple<File, Long>> audioSnippets = Arrays.stream(files)
                .map(file -> {
                    String[] split = file.getName().split("\\.");
                    if (split.length != 2) {
                        return null;
                    }
                    try {
                        long num = Long.parseLong(split[0]);
                        return new Tuple<>(file, num);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(Tuple::getB))
                .collect(Collectors.toList());

        long lastTimestamp = timestamp;

        for (int i = 0; i < audioSnippets.size(); i++) {
            Tuple<File, Long> snippet = audioSnippets.get(i);
            try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(snippet.getA())) {
                if (!audioInputStream.getFormat().matches(stereoFormat)) {
                    Voicechat.LOGGER.warn("Audio snippet {} has the wrong audio format.", snippet.getA().getName());
                    continue;
                }

                long relativeTime = snippet.getB() - lastTimestamp;

                if (relativeTime < -100L) {
                    Voicechat.LOGGER.warn("Audio snippet {} overlaps more than 100ms with previous snippet.", snippet.getA().getName());
                    continue;
                } else if (relativeTime < 0L) {
                    Voicechat.LOGGER.warn("Audio {} overlaps with previous snippet.", snippet.getA().getName());
                    relativeTime = 0L;
                }

                int silenceShorts = (int) (relativeTime * getSamplesPerMs() * stereoFormat.getChannels());

                short[] silence = new short[silenceShorts];
                encoder.encode(silence);
                short[] audio = Utils.bytesToShorts(audioInputStream.readAllBytes());
                encoder.encode(audio);
                hasAudio = true;

                lastTimestamp = snippet.getB() + getAudioTimeMillis(audio.length);

                progress.accept(((float) i + 1F) / (float) audioSnippets.size());
            }
        }
        return hasAudio;
    }

    private int getAudioTimeMillis(int audioShortLength) {
        return (audioShortLength / stereoFormat.getChannels()) / getSamplesPerMs();
    }

    private int getSamplesPerMs() {
        return ((int) stereoFormat.getSampleRate() / 1000);
    }

    private class AudioChunk {
        private final long timestamp;
        private final ByteArrayOutputStream buffer;
        private long lastTimestamp;

        public AudioChunk(long timestamp) {
            this.timestamp = timestamp;
            this.buffer = new ByteArrayOutputStream();
        }

        public void add(short[] data, long timestamp) throws IOException {
            buffer.write(Utils.shortsToBytes(data));
            this.lastTimestamp = timestamp + ((data.length * 1000L) / stereoFormat.getChannels()) / (long) stereoFormat.getSampleRate();
        }

        public byte[] getBytes() {
            return buffer.toByteArray();
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getEndTimestamp() {
            return lastTimestamp;
        }
    }

}
