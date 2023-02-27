package de.maxhenkel.voicechat.integration.clothconfig;

import de.maxhenkel.configbuilder.ConfigEntry;
import de.maxhenkel.voicechat.config.ForgeConfigBuilderWrapper;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeClothConfigIntegration extends ClothConfigIntegration {

    @Override
    protected <T> AbstractConfigListEntry<T> fromConfigEntry(ConfigEntryBuilder entryBuilder, Component name, ConfigEntry<T> entry) {
        if (!(entry instanceof ForgeConfigBuilderWrapper.ForgeConfigEntry<T> e)) {
            throw new IllegalArgumentException("Unknown config entry type %s".formatted(entry.getClass().getName()));
        }
        ForgeConfigSpec.ConfigValue<T> value = e.getValue();

        if (value instanceof ForgeConfigSpec.DoubleValue doubleValue) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startDoubleField(name, doubleValue.get())
                    .setDefaultValue(doubleValue::getDefault)
                    .setSaveConsumer(d -> {
                        doubleValue.set(d);
                        e.save();
                    })
                    .build();
        } else if (value instanceof ForgeConfigSpec.IntValue intValue) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startIntField(name, intValue.get())
                    .setDefaultValue(intValue::getDefault)
                    .setSaveConsumer(d -> {
                        intValue.set(d);
                        e.save();
                    })
                    .build();
        } else if (value instanceof ForgeConfigSpec.BooleanValue booleanValue) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startBooleanToggle(name, booleanValue.get())
                    .setDefaultValue(booleanValue::getDefault)
                    .setSaveConsumer(d -> {
                        booleanValue.set(d);
                        e.save();
                    })
                    .build();
        } else if (value.getDefault() instanceof String) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startStrField(name, (String) value.get())
                    .setDefaultValue(() -> (String) value.getDefault())
                    .setSaveConsumer(d -> {
                        value.set((T) d);
                        e.save();
                    })
                    .build();
        }

        throw new IllegalArgumentException("Unknown config entry type %s".formatted(value.getClass().getName()));
    }
}
