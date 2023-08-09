package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.CommentedProperties;
import de.maxhenkel.configbuilder.CommentedPropertyConfig;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VolumeConfig extends CommentedPropertyConfig {

    private final Map<UUID, Double> volumes;
    private final Map<String, Double> categoryVolumes;

    public VolumeConfig(Path path) {
        super(new CommentedProperties(false));
        this.path = path;
        Map<String, String> entries = getEntries();
        volumes = new HashMap<>();
        categoryVolumes = new HashMap<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            double volume = Double.parseDouble(entry.getValue().toString());
            try {
                volumes.put(UUID.fromString(entry.getKey()), volume);
            } catch (IllegalArgumentException e) {
                categoryVolumes.put(entry.getKey(), volume);
            }
        }
    }

    public double getPlayerVolume(UUID uuid, double def) {
        Double volume = volumes.get(uuid);
        if (volume == null) {
            return def;
        }
        return volume;
    }

    public double getPlayerVolume(UUID playerID) {
        return getPlayerVolume(playerID, 1D);
    }

    public double setPlayerVolume(UUID uuid, double value) {
        volumes.put(uuid, value);
        properties.set(uuid.toString(), String.valueOf(value));
        return value;
    }

    public Map<UUID, Double> getPlayerVolumes() {
        return Collections.unmodifiableMap(volumes);
    }

    public double getCategoryVolume(String category, double def) {
        Double volume = categoryVolumes.get(category);
        if (volume == null) {
            return def;
        }
        return volume;
    }

    public double getCategoryVolume(String category) {
        return getCategoryVolume(category, 1D);
    }

    public double setCategoryVolume(String category, double value) {
        categoryVolumes.put(category, value);
        properties.set(category, String.valueOf(value));
        return value;
    }

}
