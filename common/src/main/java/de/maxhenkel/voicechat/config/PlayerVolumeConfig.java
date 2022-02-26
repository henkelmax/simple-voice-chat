package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.PropertyConfig;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerVolumeConfig extends PropertyConfig {

    private final Map<UUID, Double> volumes;

    public PlayerVolumeConfig(Path path) {
        super(path);
        Map<String, Object> entries = getEntries();
        volumes = new HashMap<>(entries.size());
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            try {
                volumes.put(UUID.fromString(entry.getKey()), Double.parseDouble(entry.getValue().toString()));
            } catch (Exception e) {
            }
        }
    }

    public double getVolume(UUID uuid, double def) {
        Double volume = volumes.get(uuid);
        if (volume == null) {
            return setVolume(uuid, def);
        }
        return volume;
    }

    public double getVolume(UUID playerID) {
        return getVolume(playerID, 1D);
    }

    public double setVolume(UUID uuid, double value) {
        volumes.put(uuid, value);
        properties.put(uuid.toString(), String.valueOf(value));
        return value;
    }

    public Map<UUID, Double> getVolumes() {
        return Collections.unmodifiableMap(volumes);
    }
}
