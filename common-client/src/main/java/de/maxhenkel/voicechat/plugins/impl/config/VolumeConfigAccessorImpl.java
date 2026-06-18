package de.maxhenkel.voicechat.plugins.impl.config;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.config.VolumeConfigAccessor;
import de.maxhenkel.voicechat.config.CategoryVolumeConfig;
import de.maxhenkel.voicechat.config.PlayerVolumeConfig;

import java.util.Objects;
import java.util.UUID;

public class VolumeConfigAccessorImpl implements VolumeConfigAccessor {

    public static final VolumeConfigAccessorImpl INSTANCE = new VolumeConfigAccessorImpl();

    @Override
    public double getVolume(UUID uuid) {
        Objects.requireNonNull(uuid);
        PlayerVolumeConfig cfg = VoicechatClient.PLAYER_VOLUME_CONFIG;
        if (cfg == null) {
            return 1D;
        }
        return cfg.getVolume(uuid);
    }

    @Override
    public double getCategoryVolume(String category) {
        Objects.requireNonNull(category);
        CategoryVolumeConfig cfg = VoicechatClient.CATEGORY_VOLUME_CONFIG;
        if (cfg == null) {
            return 1D;
        }
        return cfg.getVolume(category);
    }
}
