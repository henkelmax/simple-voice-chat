package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.Config;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class ForgeConfigBuilderWrapper implements ConfigBuilder {

    private final ForgeConfigSpec.Builder builder;
    private final String[] path;

    public ForgeConfigBuilderWrapper(ForgeConfigSpec.Builder builder, String... path) {
        this.builder = builder;
        this.path = path;
    }

    @Override
    public ConfigBuilder header(String... comments) {
        // Not implemented
        return this;
    }

    @Override
    public ConfigEntry<Boolean> booleanEntry(String key, boolean def, String... comments) {
        return new ForgeConfigEntry<>(key, comments, builder.comment(comments).worldRestart().define(makePath(key), def));
    }

    @Override
    public ConfigEntry<Integer> integerEntry(String key, int def, int min, int max, String... comments) {
        return new ForgeConfigEntry<>(key, comments, builder.comment(comments).worldRestart().defineInRange(makePath(key), def, min, max));
    }

    @Override
    public ConfigEntry<Double> doubleEntry(String key, double def, double min, double max, String... comments) {
        return new ForgeConfigEntry<>(key, comments, builder.comment(comments).worldRestart().defineInRange(makePath(key), def, min, max));
    }

    @Override
    public ConfigEntry<String> stringEntry(String key, String def, String... comments) {
        return new ForgeConfigEntry<>(key, comments, builder.comment(comments).worldRestart().define(makePath(key), def));
    }

    @Override
    public ConfigEntry<List<Integer>> integerListEntry(String key, List<Integer> def, String... comments) {
        return new ForgeConfigEntry<>(key, comments, builder.comment(comments).worldRestart().define(makePath(key), def));
    }

    @Override
    public <E extends Enum<E>> ConfigEntry<E> enumEntry(String key, E def, String... comments) {
        return new ForgeConfigEntry<>(key, comments, builder.comment(comments).worldRestart().defineEnum(makePath(key), def));
    }

    private String makePath(String key) {
        ArrayList<String> pathList = new ArrayList<>(Arrays.asList(path));
        pathList.add(key);
        return String.join(".", pathList);
    }

    public class ForgeConfigEntry<T> implements ConfigEntry<T> {

        private final String[] comments;
        private final ForgeConfigSpec.ConfigValue<T> value;
        private final String key;

        public ForgeConfigEntry(String key, String[] comments, ForgeConfigSpec.ConfigValue<T> value) {
            this.key = key;
            this.comments = comments;
            this.value = value;
        }

        public ForgeConfigSpec.ConfigValue<T> getValue() {
            return value;
        }

        @Override
        public ConfigEntry<T> comment(String... comments) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getComments() {
            return comments;
        }

        @Override
        public T get() {
            return value.get();
        }

        @Override
        public ConfigEntry<T> set(T value) {
            this.value.set(value);
            return this;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public ConfigEntry<T> reset() {
            throw new UnsupportedOperationException("Can't reset Forge config value");
        }

        @Override
        public ConfigEntry<T> save() {
            value.save();
            return this;
        }

        @Override
        public ConfigEntry<T> saveSync() {
            value.save();
            return this;
        }

        @Override
        public T getDefault() {
            return ForgeConfigBuilderWrapper.getDefault(value);
        }

        @Override
        public Config getConfig() {
            return fromBuilder(builder);
        }
    }

    public static <T> T getDefault(ForgeConfigSpec.ConfigValue<T> value) {
        try {
            Field defaultSupplier = ForgeConfigSpec.ConfigValue.class.getDeclaredField("defaultSupplier");
            defaultSupplier.setAccessible(true);
            return ((Supplier<T>) defaultSupplier.get(value)).get();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get default config value", e);
        }
    }

    private static Config fromBuilder(ForgeConfigSpec.Builder builder) {
        Map<String, Object> entries;
        try {
            Field field = builder.getClass().getDeclaredField("storage");
            com.electronwill.nightconfig.core.Config config = (com.electronwill.nightconfig.core.Config) field.get(builder);
            entries = config.valueMap();
        } catch (Exception e) {
            entries = new HashMap<>();
        }
        Map<String, String> finalEntries = entries.entrySet().stream().collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().toString()), HashMap::putAll);
        return () -> finalEntries;
    }

}
