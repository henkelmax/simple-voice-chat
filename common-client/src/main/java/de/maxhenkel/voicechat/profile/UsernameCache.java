package de.maxhenkel.voicechat.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.jna.platform.win32.OaIdl;
import de.maxhenkel.voicechat.Voicechat;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.advancements.critereon.LightningStrikeTrigger;

import javax.annotation.Nullable;
import javax.print.DocFlavor;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsernameCache {

    private static final ExecutorService SAVE_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("UsernameCacheSaver");
        thread.setDaemon(true);
        return thread;
    });

    private final File file;
    private final File appendingFormattedFile;
    private final Gson gson;
    private Map<UUID, String> names;

    public UsernameCache(File file, File appendingFormattedFile) {
        this.file = file;
        this.appendingFormattedFile = appendingFormattedFile;
        this.gson = new GsonBuilder().create();
        this.names = new ConcurrentHashMap<>();
        load();
    }

    public void convert() {
        long t1 = System.nanoTime();
        try(FileOutputStream fileOutputStream = new FileOutputStream(this.appendingFormattedFile, true)) {
            for (Map.Entry<UUID, String> uuidStringEntry : this.names.entrySet()) {
                String line = uuidStringEntry.getKey().toString() + "#" + uuidStringEntry.getValue()+"\n";
                fileOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            Voicechat.LOGGER.error("Failed to convert username cache", e);
            return; // failure should not delete main cache.
        }
        if (this.file.delete()) {
            long t2 = System.nanoTime();
            Voicechat.LOGGER.debug("Username cache has been converted. " + (t2-t1)/(1_000_000.0) + " ms");
        }
    }

    public void loadAppending() {
        try {
            List<String> cacheLines = Files.readAllLines(this.appendingFormattedFile.toPath());
            for (String cacheLine : cacheLines) {
                String[] encodedData = cacheLine.split("#");
                try {
                    UUID id = UUID.fromString(encodedData[0]);
                    String userName = encodedData[1];
                    this.names.put(id, userName);
                } catch (Exception e) {
                    Voicechat.LOGGER.error("Found corrupt entry in file: " + cacheLine);
                    e.printStackTrace();
                }
            }
            Voicechat.LOGGER.debug("Loaded username cache with " + this.names.size() + " entries");
        } catch (IOException e) {
            Voicechat.LOGGER.error("Failed to load username cache", e);
        }
    }

    public void load() {
        if (this.appendingFormattedFile.exists()) {
            this.loadAppending();
            return;
        }
        if (!this.file.exists()) {
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Type usernamesType = new TypeToken<ConcurrentHashMap<UUID, String>>() {
            }.getType();
            names = gson.fromJson(reader, usernamesType);
            convert();
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to load username cache", e);
        }
        if (names == null) {
            names = new ConcurrentHashMap<>();
        }
    }

    public synchronized void saveAppending(UUID uuid, String username) {
        long t1 = System.nanoTime();
        appendingFormattedFile.getParentFile().mkdirs();
        try(FileOutputStream fileOutputStream = new FileOutputStream(this.appendingFormattedFile, true)) {
            String line = uuid + "#" + username+"\n";
            fileOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Voicechat.LOGGER.error("Failed to save username cache", e);
        }
        long t2 = System.nanoTime();
        Voicechat.LOGGER.debug("saveAppending done in " + (t2-t1)/(1_000_000.0) + " ms");
    }

    public synchronized void saveMap(Map<UUID, String> mapAppending) {
        long t1 = System.nanoTime();
        appendingFormattedFile.getParentFile().mkdirs();
        try(FileOutputStream fileOutputStream = new FileOutputStream(this.appendingFormattedFile, true)) {
            for (Map.Entry<UUID, String> uuidStringEntry : mapAppending.entrySet()) {
                String line = uuidStringEntry.getKey().toString() + "#" + uuidStringEntry.getValue()+"\n";
                fileOutputStream.write(line.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            Voicechat.LOGGER.error("Failed to save username cache", e);
        }
        long t2 = System.nanoTime();
        Voicechat.LOGGER.debug("saveMap done in " + (t2-t1)/(1_000_000.0) + " ms, entries: " + (mapAppending.size()));
    }

    @Nullable
    public String getUsername(UUID uuid) {
        return names.get(uuid);
    }

    public boolean has(UUID uuid) {
        return names.containsKey(uuid);
    }

    public void updateUsername(UUID uuid, String name) {
        names.put(uuid, name);
    }

    public void updateUsernameAndSave(UUID uuid, String name) {
        @Nullable String oldName = names.get(uuid);
        if (!name.equals(oldName)) {
            names.put(uuid, name);
            SAVE_EXECUTOR_SERVICE.execute(() -> saveAppending(uuid, name));
        }
    }

}
