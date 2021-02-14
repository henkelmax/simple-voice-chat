package de.maxhenkel.voicechat.config;

import net.minecraft.entity.player.PlayerEntity;

import java.nio.file.Path;
import java.util.UUID;

public class PlayerVolumeConfig extends Config {

    public PlayerVolumeConfig(Path path) {
        super(path);
    }

    public double getVolume(UUID uuid, double def) {
        String property = properties.getProperty(uuid.toString());
        if (property == null) {
            return setVolume(uuid, def);
        }
        try {
            return Double.parseDouble(property);
        } catch (NumberFormatException e) {
            return setVolume(uuid, def);
        }
    }

    public double getVolume(PlayerEntity playerEntity) {
        return getVolume(playerEntity.getUuid(), 1D);
    }

    public double setVolume(UUID uuid, double value) {
        properties.put(uuid.toString(), String.valueOf(value));
        return value;
    }
}
