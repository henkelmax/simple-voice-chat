package de.maxhenkel.voicechat.voice.client;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.*;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Tuple;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Nullable
    private final GameProfileCache gameProfileCache;
    private final Map<UUID, AudioChunk> chunks;

    private final AudioFormat stereoFormat;

    private final ExecutorService threadPool;

    public AudioRecorder() {
        timestamp = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String recordingDestination = VoicechatClient.CLIENT_CONFIG.recordingDestination.get();
        if (recordingDestination.trim().isEmpty()) {
            location = CommonCompatibilityManager.INSTANCE.getGameDirectory().resolve("voicechat_recordings").resolve(FORMAT.format(cal.getTime()));
        } else {
            location = Paths.get(recordingDestination).resolve(FORMAT.format(cal.getTime()));
        }

        location.toFile().mkdirs();
        chunks = new ConcurrentHashMap<>();
        gameProfileCache = GameProfileUtils.getGameProfileCache();

        stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SoundManager.SAMPLE_RATE, 16, 2, 4, SoundManager.SAMPLE_RATE, false);

        threadPool = Executors.newSingleThreadExecutor();
    }

    public Path getLocation() {
        return location;
    }

    public long getStartTime() {
        return timestamp;
    }

    public int getRecordedPlayerCount() {
        return chunks.size();
    }

    public String getDuration() {
        long duration = System.currentTimeMillis() - timestamp;
        DateFormat fmt = new SimpleDateFormat(":mm:ss");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return (duration / (1000L * 60L * 60L)) + fmt.format(new Date(duration));
    }

    public String getStorage() {
        return FileUtils.byteCountToDisplaySize((System.currentTimeMillis() - timestamp) * (long) stereoFormat.getFrameSize() * ((long) stereoFormat.getFrameRate() / 1000L) * getRecordedPlayerCount());
    }

    private String lookupName(UUID uuid) {
        if (gameProfileCache == null) {
            return uuid.toString();
        }
        return gameProfileCache.get(uuid).map(GameProfile::getName).orElse("system-" + uuid);
    }

    private Path getFilePath(UUID playerUUID, long timestamp) {
        return location.resolve(playerUUID.toString()).resolve(timestamp + ".wav");
    }

    public void appendChunk(UUID uuid, long timestamp, short[] data) throws IOException {
        getChunk(uuid, timestamp).add(data);
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

    public void writeChunkThreaded(UUID playerUUID) {
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
        save();
        threadPool.shutdown();
    }

    public void save() {
        threadPool.execute(() -> {
            send(new TranslatableComponent("message.voicechat.processing_recording_session"));
            try {
                flush();
                AtomicLong time = new AtomicLong();
                convert(progress -> {
                    if (progress >= 1F || System.currentTimeMillis() - time.get() > 1000L) {
                        send(new TranslatableComponent("message.voicechat.processing_progress",
                                new TextComponent(String.valueOf((int) (progress * 100F)))
                                        .withStyle(ChatFormatting.GRAY))
                        );
                        time.set(System.currentTimeMillis());
                    }
                });
                send(new TranslatableComponent("message.voicechat.save_session",
                        new TextComponent(location.normalize().toString())
                                .withStyle(ChatFormatting.GRAY)
                                .withStyle(style -> style
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("message.voicechat.open_folder")))
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, location.normalize().toString()))))
                );
            } catch (Exception e) {
                e.printStackTrace();
                send(new TranslatableComponent("message.voicechat.save_session_failed", e.getMessage()));
            }
        });
    }

    private void send(Component msg) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && mc.level != null) {
            player.sendMessage(msg, Util.NIL_UUID);
        } else {
            Voicechat.LOGGER.info(msg.getString());
        }
    }

    private void convert(Consumer<Float> progress) throws UnsupportedAudioFileException, IOException {
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
            RandomAccessAudio audio = convertFiles(files, p -> progress.accept(progressPerc + p * (1F / (float) directories.length)));
            if (audio == null) {
                return;
            }
            writeWav(Utils.shortsToBytes(audio.getShorts()), audio.getAudioFormat(), location.resolve(lookupName(uuid) + ".wav").toFile());
            FileUtils.deleteDirectory(userDir);
        }
    }

    @Nullable
    private RandomAccessAudio convertFiles(File[] files, Consumer<Float> progress) throws UnsupportedAudioFileException, IOException {
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
                .collect(Collectors.toList());

        RandomAccessAudio audio = null;
        for (int i = 0; i < audioSnippets.size(); i++) {
            Tuple<File, Long> snippet = audioSnippets.get(i);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(snippet.getA());
            if (audio == null) {
                audio = new RandomAccessAudio(audioInputStream.getFormat());
            } else if (!audioInputStream.getFormat().matches(audio.getAudioFormat())) {
                Voicechat.LOGGER.warn("Audio snippet {} has the wrong audio format.", snippet.getA().getName());
                continue;
            }

            int ts = (int) (snippet.getB() - timestamp);
            audio.insertAt(Utils.bytesToShorts(audioInputStream.readAllBytes()), ts);
            audioInputStream.close();
            progress.accept(((float) i + 1F) / (float) audioSnippets.size());
        }

        return audio;
    }

    private static class AudioChunk {
        private final long timestamp;
        private final ByteArrayOutputStream buffer;

        public AudioChunk(long timestamp) {
            this.timestamp = timestamp;
            this.buffer = new ByteArrayOutputStream();
        }

        public void add(short[] data) throws IOException {
            buffer.write(Utils.shortsToBytes(data));
        }

        public byte[] getBytes() {
            return buffer.toByteArray();
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private static class RandomAccessAudio {

        private final AudioFormat audioFormat;
        private final DynamicShortArray data;

        public RandomAccessAudio(AudioFormat audioFormat) {
            this.audioFormat = audioFormat;
            this.data = new DynamicShortArray();
        }

        public void insertAt(short[] shorts, int offsetMilliseconds) throws UnsupportedAudioFileException {
            if (audioFormat.getFrameSize() == AudioSystem.NOT_SPECIFIED) {
                throw new UnsupportedAudioFileException("Frame size not specified");
            }
            if (audioFormat.getChannels() == AudioSystem.NOT_SPECIFIED) {
                throw new UnsupportedAudioFileException("Channel count not specified");
            }
            if (audioFormat.getSampleRate() == AudioSystem.NOT_SPECIFIED) {
                throw new UnsupportedAudioFileException("Sample rate not specified");
            }
            int shortsPerMs = ((int) audioFormat.getSampleRate() / 1000);
            data.add(shorts, offsetMilliseconds * shortsPerMs * audioFormat.getChannels());
        }

        public AudioFormat getAudioFormat() {
            return audioFormat;
        }

        public short[] getShorts() {
            return data.getShorts();
        }
    }

    private static class DynamicShortArray {
        private short[] data;

        public DynamicShortArray() {
            this(0);
        }

        public DynamicShortArray(int initialLength) {
            data = new short[initialLength];
        }

        public DynamicShortArray(short[] initialData) {
            data = initialData;
        }

        public void add(short[] shorts, int offset) {
            int max = shorts.length + offset;
            if (max > data.length) {
                short[] newData = new short[max];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }

            System.arraycopy(shorts, 0, data, offset, shorts.length);
        }

        public short[] getShorts() {
            return data;
        }
    }

}
