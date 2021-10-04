package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.Config;
import de.maxhenkel.configbuilder.ConfigEntry;
import net.minecraftforge.common.ForgeConfigSpec;

public class DummyEntry<T> implements ConfigEntry<T> {

    private final T value;
    private final ForgeConfigSpec.Builder builder;

    public DummyEntry(T value, ForgeConfigSpec.Builder builder) {
        this.value = value;
        this.builder = builder;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public ConfigEntry<T> set(T t) {
        return this;
    }

    @Override
    public ConfigEntry<T> reset() {
        return this;
    }

    @Override
    public ConfigEntry<T> save() {
        return this;
    }

    @Override
    public ConfigEntry<T> saveSync() {
        return this;
    }

    @Override
    public Config getConfig() {
        return ForgeServerConfig.fromBuilder(builder);
    }
}
