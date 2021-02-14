package de.maxhenkel.voicechat.config;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfigBuilder {

    protected List<ConfigEntry<?>> entries;

    private ConfigBuilder() {
        entries = new ArrayList<>();
    }

    public static void create(Path path, Consumer<ConfigBuilder> builderConsumer) {
        ConfigBuilder builder = new ConfigBuilder();
        builderConsumer.accept(builder);
        Config config = new Config(path);
        for (ConfigEntry<?> entry : builder.entries) {
            entry.config = config;
            entry.loadOrDefault();
        }
        config.save();
    }

    public ConfigEntry<Integer> integerEntry(String key, int def, int min, int max) {
        IntegerConfigEntry entry = new IntegerConfigEntry(min, max);
        entry.key = key;
        entry.def = def;
        entries.add(entry);
        return entry;
    }

    public ConfigEntry<Double> doubleEntry(String key, double def, double min, double max) {
        DoubleConfigEntry entry = new DoubleConfigEntry(min, max);
        entry.key = key;
        entry.def = def;
        entries.add(entry);
        return entry;
    }

    public ConfigEntry<String> stringEntry(String key, String def) {
        StringConfigEntry entry = new StringConfigEntry();
        entry.key = key;
        entry.def = def;
        entries.add(entry);
        return entry;
    }

    public <T extends Enum> ConfigEntry<T> enumEntry(String key, T def) {
        EnumConfigEntry<T> entry = new EnumConfigEntry(def.getClass());
        entry.key = key;
        entry.def = def;
        entries.add(entry);
        return (ConfigEntry<T>) entry;
    }

    public static abstract class ConfigEntry<T> {
        protected Config config;
        protected String key;
        protected T value, def;

        private ConfigEntry() {

        }

        public T get() {
            return value;
        }

        public void set(T value) {
            if (this.value.equals(value)) {
                return;
            }
            this.value = value;
            String serialized = serialize(value);
            config.getProperties().setProperty(key, serialized);
        }

        protected void loadOrDefault() {
            if (config.getProperties().containsKey(key)) {
                T val = deserialize(config.getProperties().getProperty(key));
                if (val == null) {
                    reset();
                } else {
                    value = val;
                }
            } else {
                reset();
            }
        }

        public void reset() {
            value = def;
            config.getProperties().setProperty(key, serialize(def));
        }

        public void save() {
            config.save();
        }

        @Nullable
        public abstract T deserialize(String str);

        public abstract String serialize(T val);
    }

    private static class IntegerConfigEntry extends ConfigEntry<Integer> {

        private int min, max;

        public IntegerConfigEntry(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        @Nullable
        public Integer deserialize(String str) {
            try {
                return Math.max(Math.min(Integer.parseInt(str), max), min);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String serialize(Integer val) {
            return String.valueOf(val);
        }
    }

    private static class DoubleConfigEntry extends ConfigEntry<Double> {

        private double min, max;

        public DoubleConfigEntry(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        @Nullable
        public Double deserialize(String str) {
            try {
                return Math.max(Math.min(Double.parseDouble(str), max), min);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public String serialize(Double val) {
            return String.valueOf(val);
        }
    }

    private static class StringConfigEntry extends ConfigEntry<String> {
        @Override
        @Nullable
        public String deserialize(String str) {
            return str;
        }

        @Override
        public String serialize(String val) {
            return val;
        }
    }

    private static class EnumConfigEntry<T extends Enum> extends ConfigEntry<Enum> {
        protected Class<T> enumClass;

        public EnumConfigEntry(Class<T> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        @Nullable
        public Enum deserialize(String str) {
            try {
                return Enum.valueOf(enumClass, str);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String serialize(Enum val) {
            return val.name();
        }
    }

}
