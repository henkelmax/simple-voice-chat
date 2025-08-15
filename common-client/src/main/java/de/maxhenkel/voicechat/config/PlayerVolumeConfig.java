package de.maxhenkel.voicechat.config;

import java.nio.file.Path;
import java.util.UUID;

public class PlayerVolumeConfig extends VolumeConfigBase<UUID> {

    public PlayerVolumeConfig(Path path) {
        super(path);
    }

    @Override
    protected String getConfigName() {
        return "player";
    }

    @Override
    protected UUID mapKey(String key) {
        return UUID.fromString(key);
    }

    @Override
    protected String serializeKey(UUID key) {
        return key.toString();
    }

}
