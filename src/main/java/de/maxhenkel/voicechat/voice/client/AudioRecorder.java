package de.maxhenkel.voicechat.voice.client;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.voicechat.Main;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.*;
import net.minecraft.util.Tuple;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AudioRecorder {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
    private static final GameProfile SYSTEM = new GameProfile(new UUID(0L, 0L), "system");

    private final long timestamp;
    private final Path location;
    private final Client client;

    private Map<UUID, AudioChunk> chunks;
    private Map<UUID, GameProfile> gameProfileLookup;

    public AudioRecorder(Client client) {
        this.client = client;
        timestamp = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String recordingDestination = Main.CLIENT_CONFIG.recordingDestination.get();
        if (recordingDestination.trim().isEmpty()) {
            location = Minecraft.getInstance().gameDirectory.toPath().resolve("voicechat_recordings").resolve(FORMAT.format(cal.getTime()));
        } else {
            location = Paths.get(recordingDestination);
        }

        location.toFile().mkdirs();
        chunks = new ConcurrentHashMap<>();
        gameProfileLookup = new ConcurrentHashMap<>();
    }

    public Path getLocation() {
        return location;
    }

    public long getStartTime() {
        return timestamp;
    }

    public int getRecordedPlayerCount() {
        return gameProfileLookup.size();
    }

    public Client getClient() {
        return client;
    }

    public String getDuration() {
        long duration = System.currentTimeMillis() - timestamp;
        DateFormat fmt = new SimpleDateFormat(":mm:ss");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return (duration / (1000L * 60L * 60L)) + fmt.format(new Date(duration));
    }

    public String getStorage() {
        AudioFormat format = AudioChannelConfig.getStereoFormat();
        return FileUtils.byteCountToDisplaySize((System.currentTimeMillis() - timestamp) * (long) format.getFrameSize() * ((long) format.getFrameRate() / 1000L) * getRecordedPlayerCount());
    }

    private String lookupName(UUID uuid) {
        GameProfile gameProfile = gameProfileLookup.get(uuid);
        if (gameProfile == null) {
            return uuid.toString();
        }
        return gameProfile.getName();
    }

    private Path getFilePath(UUID playerUUID, long timestamp) {
        return location.resolve(playerUUID.toString()).resolve(timestamp + ".wav");
    }

    public void appendChunk(@Nullable GameProfile profile, long timestamp, byte[] data) throws IOException {
        GameProfile p = profile != null ? profile : SYSTEM;
        gameProfileLookup.putIfAbsent(p.getId(), p);
        getChunk(p.getId(), timestamp).add(data);
    }

    private void writeChunk(UUID playerUUID, AudioChunk chunk) throws IOException {
        File file = getFilePath(playerUUID, chunk.getTimestamp()).toFile();
        file.getParentFile().mkdirs();
        byte[] data = chunk.getBytes();
        writeWav(data, AudioChannelConfig.getStereoFormat(), file);
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
        //TODO Thread pooling
        new Thread(() -> {
            try {
                writeChunk(playerUUID, chunk);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Nullable
    private AudioChunk getAndRemoveChunk(UUID playerUUID) {
        return chunks.remove(playerUUID);
    }

    private AudioChunk getChunk(UUID playerUUID, long timestamp) {
        if (!chunks.containsKey(playerUUID)) {
            AudioChunk chunk = new AudioChunk(timestamp);
            chunks.put(playerUUID, chunk);
            return chunk;
        } else {
            return chunks.get(playerUUID);
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
    }

    public void save() {
        new Thread(() -> {
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
        }).start();
    }

    private void send(Component msg) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && mc.level != null) {
            player.sendMessage(msg, Util.NIL_UUID);
        } else {
            Main.LOGGER.info(msg.getString());
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
            writeWav(audio.getBytes(), audio.getAudioFormat(), location.resolve(lookupName(uuid) + ".wav").toFile());
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

            FileInputStream fis = new FileInputStream(snippet.getA());
            BufferedInputStream bis = new BufferedInputStream(fis);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);

            if (audio == null) {
                audio = new RandomAccessAudio(audioInputStream.getFormat());
            } else if (!audioInputStream.getFormat().matches(audio.getAudioFormat())) {
                Main.LOGGER.warn("Audio snippet {} has the wrong audio format.", snippet.getA().getName());
                continue;
            }

            int ts = (int) (snippet.getB() - timestamp);
            audio.insertAt(IOUtils.toByteArray(audioInputStream), ts);
            audioInputStream.close();
            bis.close();
            fis.close();
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

        public void add(byte[] data) throws IOException {
            buffer.write(data);
        }

        public byte[] getBytes() {
            return buffer.toByteArray();
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private static class RandomAccessAudio {

        private AudioFormat audioFormat;
        private DynamicByteArray data;

        public RandomAccessAudio(AudioFormat audioFormat) {
            this.audioFormat = audioFormat;
            this.data = new DynamicByteArray();
        }

        public void insertAt(byte[] b, int offsetMilliseconds) throws UnsupportedAudioFileException {
            if (audioFormat.getFrameSize() == AudioSystem.NOT_SPECIFIED) {
                throw new UnsupportedAudioFileException("Frame size not specified");
            }
            if (audioFormat.getChannels() == AudioSystem.NOT_SPECIFIED) {
                throw new UnsupportedAudioFileException("Channel count not specified");
            }
            if (audioFormat.getSampleRate() == AudioSystem.NOT_SPECIFIED) {
                throw new UnsupportedAudioFileException("Sample rate not specified");
            }
            int bytesPerMs = ((int) audioFormat.getSampleRate() / 1000) * audioFormat.getFrameSize();
            data.add(b, offsetMilliseconds * bytesPerMs);
        }

        public AudioFormat getAudioFormat() {
            return audioFormat;
        }

        public byte[] getBytes() {
            return data.getBytes();
        }
    }

    private static class DynamicByteArray {
        private byte[] data;

        public DynamicByteArray() {
            this(0);
        }

        public DynamicByteArray(int initialLength) {
            data = new byte[initialLength];
        }

        public DynamicByteArray(byte[] initialData) {
            data = initialData;
        }

        public void add(byte[] b, int offset) {
            int max = b.length + offset;
            if (max > data.length) {
                byte[] newData = new byte[max];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }

            System.arraycopy(b, 0, data, offset, b.length);
        }

        public byte[] getBytes() {
            return data;
        }
    }

}
