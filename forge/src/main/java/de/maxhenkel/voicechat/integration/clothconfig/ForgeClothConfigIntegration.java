package de.maxhenkel.voicechat.integration.clothconfig;

import de.maxhenkel.configbuilder.ConfigEntry;
import de.maxhenkel.voicechat.config.ForgeConfigBuilderWrapper;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class ForgeClothConfigIntegration extends ClothConfigIntegration {

    @Override
    protected <T> AbstractConfigListEntry<T> fromConfigEntry(ConfigEntryBuilder entryBuilder, ITextComponent name, ConfigEntry<T> entry) {
        if (!(entry instanceof ForgeConfigBuilderWrapper.ForgeConfigEntry)) {
            throw new IllegalArgumentException(String.format("Unknown config entry type %s", entry.getClass().getName()));
        }
        ForgeConfigBuilderWrapper.ForgeConfigEntry<T> e = (ForgeConfigBuilderWrapper.ForgeConfigEntry<T>) entry;
        ForgeConfigSpec.ConfigValue<T> value = e.getValue();

        if (value instanceof ForgeConfigSpec.DoubleValue) {
            ForgeConfigSpec.DoubleValue doubleValue = (ForgeConfigSpec.DoubleValue) value;
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startDoubleField(name, doubleValue.get())
                    .setDefaultValue(getDefault(doubleValue))
                    .setSaveConsumer(d -> {
                        doubleValue.set(d);
                        e.save();
                    })
                    .build();
        } else if (value instanceof ForgeConfigSpec.IntValue) {
            ForgeConfigSpec.IntValue intValue = (ForgeConfigSpec.IntValue) value;
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startIntField(name, intValue.get())
                    .setDefaultValue(getDefault(intValue))
                    .setSaveConsumer(d -> {
                        intValue.set(d);
                        e.save();
                    })
                    .build();
        } else if (value instanceof ForgeConfigSpec.BooleanValue) {
            ForgeConfigSpec.BooleanValue booleanValue = (ForgeConfigSpec.BooleanValue) value;
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startBooleanToggle(name, booleanValue.get())
                    .setDefaultValue(getDefault(booleanValue))
                    .setSaveConsumer(d -> {
                        booleanValue.set(d);
                        e.save();
                    })
                    .build();
        } else if (getDefault(value) instanceof String) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startStrField(name, (String) value.get())
                    .setDefaultValue(() -> (String) getDefault(value))
                    .setSaveConsumer(d -> {
                        value.set((T) d);
                        e.save();
                    })
                    .build();
        }

        throw new IllegalArgumentException(String.format("Unknown config entry type %s", value.getClass().getName()));
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

}
