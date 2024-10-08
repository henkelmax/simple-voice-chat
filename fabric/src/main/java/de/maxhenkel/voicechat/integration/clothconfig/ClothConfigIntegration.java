package de.maxhenkel.voicechat.integration.clothconfig;

import de.maxhenkel.configbuilder.entry.*;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.integration.freecam.FreecamMode;
import de.maxhenkel.voicechat.voice.client.GroupPlayerIconOrientation;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ClothConfigIntegration {

    public static final MutableComponent SETTINGS = Component.translatable("cloth_config.voicechat.settings");
    public static final MutableComponent OTHER_SETTINGS = Component.translatable("cloth_config.voicechat.category.ingame_menu");

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder
                .create()
                .setParentScreen(parent)
                .setTitle(SETTINGS);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("cloth_config.voicechat.category.general"));

        general.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.recordingDestination));
        general.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.runLocalServer));
        general.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment));
        general.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("cloth_config.voicechat.config.freecam_mode"), FreecamMode.class, VoicechatClient.CLIENT_CONFIG.freecamMode.get())
                .setEnumNameProvider(e -> Component.translatable(String.format("cloth_config.voicechat.config.freecam_mode.%s", e.name().toLowerCase())))
                .setTooltip(Component.translatable("cloth_config.voicechat.config.freecam_mode.description"))
                .setDefaultValue(VoicechatClient.CLIENT_CONFIG.freecamMode::getDefault)
                .setSaveConsumer(e -> VoicechatClient.CLIENT_CONFIG.freecamMode.set(e).save())
                .build()
        );
        general.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.muteOnJoin));

        ConfigCategory audio = builder.getOrCreateCategory(Component.translatable("cloth_config.voicechat.category.audio"));
        audio.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.audioPacketThreshold));
        audio.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.deactivationDelay));
        audio.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.outputBufferSize));

        ConfigCategory hudIcons = builder.getOrCreateCategory(Component.translatable("cloth_config.voicechat.category.hud_icons"));
        hudIcons.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.hudIconScale));
        hudIcons.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.hudIconPosX));
        hudIcons.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.hudIconPosY));

        ConfigCategory groupIcons = builder.getOrCreateCategory(Component.translatable("cloth_config.voicechat.category.group_chat_icons"));
        groupIcons.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("cloth_config.voicechat.config.group_player_icon_orientation"), GroupPlayerIconOrientation.class, VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation.get())
                .setEnumNameProvider(e -> Component.translatable(String.format("cloth_config.voicechat.config.group_player_icon_orientation.%s", e.name().toLowerCase())))
                .setTooltip(Component.translatable("cloth_config.voicechat.config.group_player_icon_orientation.description"))
                .setDefaultValue(VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation::getDefault)
                .setSaveConsumer(e -> VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation.set(e).save())
                .build()
        );
        groupIcons.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.groupHudIconScale));
        groupIcons.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosX));
        groupIcons.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosY));
        groupIcons.addEntry(fromConfigEntry(entryBuilder, VoicechatClient.CLIENT_CONFIG.showOwnGroupIcon));

        builder.getOrCreateCategory(OTHER_SETTINGS);
        return builder.build();
    }

    protected static <T> AbstractConfigListEntry<T> fromConfigEntry(ConfigEntryBuilder entryBuilder, ConfigEntry<T> entry) {
        Component name = Component.translatable(String.format("cloth_config.voicechat.config.%s", entry.getKey()));
        Component description = Component.translatable(String.format("cloth_config.voicechat.config.%s.description", entry.getKey()));

        if (entry instanceof DoubleConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startDoubleField(name, e.get())
                    .setTooltip(description)
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> {
                        e.set(d);
                        e.save();
                    })
                    .build();
        } else if (entry instanceof IntegerConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startIntField(name, e.get())
                    .setTooltip(description)
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        } else if (entry instanceof BooleanConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startBooleanToggle(name, e.get())
                    .setTooltip(description)
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        } else if (entry instanceof StringConfigEntry e) {
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startStrField(name, e.get())
                    .setTooltip(description)
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        }

        throw new IllegalArgumentException("Unknown config entry type %s".formatted(entry.getClass().getName()));
    }

}
