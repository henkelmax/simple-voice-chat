package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

public class PaperTranslations extends Translations {

    public final ConfigEntry<String> placeholderVoicechatInstalled;
    public final ConfigEntry<String> placeholderVoicechatNotInstalled;
    public final ConfigEntry<String> placeholderVoicechatDisabled;

    public PaperTranslations(ConfigBuilder builder) {
        super(builder);
        placeholderVoicechatInstalled = builder.stringEntry(
                "placeholder_api_voicechat_installed",
                "",
                "The Placeholder API string for '%voicechat_installed%'"
        );
        placeholderVoicechatNotInstalled = builder.stringEntry(
                "placeholder_api_voicechat_not_installed",
                "",
                "The Placeholder API string for '%voicechat_not_installed%'"
        );
        placeholderVoicechatDisabled = builder.stringEntry(
                "placeholder_api_voicechat_disabled",
                "",
                "The Placeholder API string for '%voicechat_disabled%'"
        );
    }

}
