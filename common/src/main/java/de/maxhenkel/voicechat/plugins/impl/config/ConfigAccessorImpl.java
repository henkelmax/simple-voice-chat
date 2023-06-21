package de.maxhenkel.voicechat.plugins.impl.config;

import de.maxhenkel.configbuilder.Config;
import de.maxhenkel.voicechat.api.config.ConfigAccessor;

import javax.annotation.Nullable;

public class ConfigAccessorImpl implements ConfigAccessor {

    private Config config;

    public ConfigAccessorImpl(Config config) {
        this.config = config;
    }

    @Override
    public boolean hasKey(String key) {
        return config.getEntries().containsKey(key);
    }

    @Nullable
    @Override
    public String getValue(String key) {
        return config.getEntries().get(key);
    }

}
