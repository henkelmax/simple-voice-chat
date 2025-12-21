package de.maxhenkel.voicechat.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class UsernameCache {

    private static final ExecutorService SAVE_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("UsernameCacheSaver");
        thread.setDaemon(true);
        return thread;
    });

    private final File file;
    private final Gson gson;
    private Map<UUID, String> names;

    public UsernameCache(File file) {
        this.file = file;
        this.gson = new GsonBuilder().create();
        this.names = new ConcurrentHashMap<>();
        load();
    }

    public void load() {
        if (!file.exists()) {
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Type usernamesType = new TypeToken<ConcurrentHashMap<UUID, String>>() {
            }.getType();
            names = gson.fromJson(reader, usernamesType);
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to load username cache", e);
        }
        if (names == null) {
            names = new ConcurrentHashMap<>();
        }
    }

    public synchronized void save() {
        long time = System.currentTimeMillis();
        file.getParentFile().mkdirs();
        Set<UUID> volumeIds = VoicechatClient.PLAYER_VOLUME_CONFIG.getVolumes().keySet();

        Map<UUID, String> usernamesToSave = names.entrySet()
                .stream()
                .filter(entry -> volumeIds.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Voicechat.LOGGER.debug("Reduced cached usernames to save from {} to {}", names.size(), usernamesToSave.size());

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(usernamesToSave, writer);
            Voicechat.LOGGER.debug("Saved username cache in {}ms", System.currentTimeMillis() - time);
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to save username cache", e);
        }
    }

    public void saveAsync() {
        SAVE_EXECUTOR_SERVICE.execute(this::save);
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
            if (VoicechatClient.PLAYER_VOLUME_CONFIG.contains(uuid)) {
                saveAsync();
            }
        }
    }

}
