package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.CommentedProperties;
import de.maxhenkel.configbuilder.CommentedPropertyConfig;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;

import java.nio.file.Path;
import java.util.*;

public abstract class VolumeConfigBase<T> extends CommentedPropertyConfig {

    protected final Map<T, Double> volumes;

    public VolumeConfigBase(Path path) {
        super(new CommentedProperties(false));
        this.path = path;
        reload();
        properties.setHeaderComments(Collections.singletonList(String.format("%s %s volume config", CommonCompatibilityManager.INSTANCE.getModName(), getConfigName())));
        Map<String, String> entries = getEntries();
        volumes = new HashMap<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            properties.setComments(entry.getKey(), Collections.emptyList());
            try {
                double volume = Double.parseDouble(entry.getValue());
                try {
                    volumes.put(mapKey(entry.getKey()), volume);
                } catch (Exception e) {
                    Voicechat.LOGGER.warn("Invalid volume key '{}'", entry.getKey());
                }
            } catch (NumberFormatException e) {
                Voicechat.LOGGER.warn("Invalid volume value '{}' for '{}'", entry.getValue(), entry.getKey());
                properties.remove(entry.getKey());
            }
        }
        saveSync();
    }

    protected abstract String getConfigName();

    protected abstract T mapKey(String key) throws Exception;

    protected abstract String serializeKey(T key);

    protected Double getDefaultValue() {
        return 1D;
    }

    public double getVolume(T key, double def) {
        Double volume = volumes.get(key);
        if (volume == null) {
            return def;
        }
        return volume;
    }

    public double getVolume(T key) {
        return getVolume(key, getDefaultValue());
    }

    public double setVolume(T key, double value, String... comments) {
        volumes.put(key, value);
        properties.set(serializeKey(key), String.format(Locale.ROOT, "%.3f", value), comments);
        return value;
    }

    public boolean contains(T key) {
        return volumes.containsKey(key);
    }

    @Override
    public void save() {
        super.save();
        VoicechatClient.USERNAME_CACHE.saveAsync();
    }

    public Map<T, Double> getVolumes() {
        return volumes;
    }

}
