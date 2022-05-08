package de.maxhenkel.voicechat.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.maxhenkel.voicechat.Voicechat;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameCache {

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
            Voicechat.LOGGER.error("Failed to load username cache: {}", e.getMessage());
        }
        if (names == null) {
            names = new ConcurrentHashMap<>();
        }
    }

    public void save() {
        file.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(names, writer);
        } catch (Exception e) {
            Voicechat.LOGGER.error("Failed to save username cache: {}", e.getMessage());
        }
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
            save();
        }
    }

}
