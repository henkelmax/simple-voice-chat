package de.maxhenkel.voicechat.integration.clothconfig;

import de.maxhenkel.configbuilder.entry.*;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.GroupPlayerIconOrientation;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ClothConfigIntegration {

    public static final TranslationTextComponent SETTINGS = new TranslationTextComponent("cloth_config.voicechat.settings");
    public static final TranslationTextComponent OTHER_SETTINGS = new TranslationTextComponent("cloth_config.voicechat.category.other");

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder
                .create()
                .setParentScreen(parent)
                .setTitle(SETTINGS);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(new TranslationTextComponent("cloth_config.voicechat.category.general"));

        general.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.recording_destination"), VoicechatClient.CLIENT_CONFIG.recordingDestination));
        general.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.run_local_server"), VoicechatClient.CLIENT_CONFIG.runLocalServer));
        general.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.offline_player_volume_adjustment"), VoicechatClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment));
        general.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.freecam_support"), VoicechatClient.CLIENT_CONFIG.freecamSupport));
        general.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.mute_on_join"), VoicechatClient.CLIENT_CONFIG.muteOnJoin));

        ConfigCategory audio = builder.getOrCreateCategory(new TranslationTextComponent("cloth_config.voicechat.category.audio"));
        audio.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.audio_packet_threshold"), VoicechatClient.CLIENT_CONFIG.audioPacketThreshold));
        audio.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.deactivation_delay"), VoicechatClient.CLIENT_CONFIG.deactivationDelay));
        audio.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.output_buffer_size"), VoicechatClient.CLIENT_CONFIG.outputBufferSize));

        ConfigCategory hudIcons = builder.getOrCreateCategory(new TranslationTextComponent("cloth_config.voicechat.category.hud_icons"));
        hudIcons.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.hud_icon_scale"), VoicechatClient.CLIENT_CONFIG.hudIconScale));
        hudIcons.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.hud_icon_x"), VoicechatClient.CLIENT_CONFIG.hudIconPosX));
        hudIcons.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.hud_icon_y"), VoicechatClient.CLIENT_CONFIG.hudIconPosY));

        ConfigCategory groupIcons = builder.getOrCreateCategory(new TranslationTextComponent("cloth_config.voicechat.category.group_chat_icons"));
        groupIcons.addEntry(entryBuilder
                .startEnumSelector(new TranslationTextComponent("cloth_config.voicechat.config.group_player_icon_orientation"), GroupPlayerIconOrientation.class, VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation.get())
                .setDefaultValue(VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation::getDefault)
                .setSaveConsumer(e -> VoicechatClient.CLIENT_CONFIG.groupPlayerIconOrientation.set(e).save())
                .build()
        );
        groupIcons.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.group_hud_icon_scale"), VoicechatClient.CLIENT_CONFIG.groupHudIconScale));
        groupIcons.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.group_player_icon_pos_x"), VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosX));
        groupIcons.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.group_player_icon_pos_y"), VoicechatClient.CLIENT_CONFIG.groupPlayerIconPosY));
        groupIcons.addEntry(fromConfigEntry(entryBuilder, new TranslationTextComponent("cloth_config.voicechat.config.show_own_group_icon"), VoicechatClient.CLIENT_CONFIG.showOwnGroupIcon));

        builder.getOrCreateCategory(OTHER_SETTINGS);
        return builder.build();
    }

    protected static <T> AbstractConfigListEntry<T> fromConfigEntry(ConfigEntryBuilder entryBuilder, ITextComponent name, ConfigEntry<T> entry) {
        if (entry instanceof DoubleConfigEntry) {
            DoubleConfigEntry e = (DoubleConfigEntry) entry;
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startDoubleField(name, e.get())
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> {
                        e.set(d);
                        e.save();
                    })
                    .build();
        } else if (entry instanceof IntegerConfigEntry) {
            de.maxhenkel.configbuilder.ConfigBuilderImpl.IntegerConfigEntry e = (de.maxhenkel.configbuilder.ConfigBuilderImpl.IntegerConfigEntry) entry;
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startIntField(name, e.get())
                    .setMin(e.getMin())
                    .setMax(e.getMax())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        } else if (entry instanceof BooleanConfigEntry) {
            de.maxhenkel.configbuilder.ConfigBuilderImpl.BooleanConfigEntry e = (de.maxhenkel.configbuilder.ConfigBuilderImpl.BooleanConfigEntry) entry;
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startBooleanToggle(name, e.get())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        } else if (entry instanceof StringConfigEntry) {
            de.maxhenkel.configbuilder.ConfigBuilderImpl.StringConfigEntry e = (de.maxhenkel.configbuilder.ConfigBuilderImpl.StringConfigEntry) entry;
            return (AbstractConfigListEntry<T>) entryBuilder
                    .startStrField(name, e.get())
                    .setDefaultValue(e::getDefault)
                    .setSaveConsumer(d -> e.set(d).save())
                    .build();
        }

        throw new IllegalArgumentException(String.format("Unknown config entry type %s", entry.getClass().getName()));
    }

}
