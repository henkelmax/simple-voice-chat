package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

public class Translations {

    public final ConfigEntry<String> forceVoicechatKickMessage;
    public final ConfigEntry<String> voicechatNotCompatibleMessage;
    public final ConfigEntry<String> voicechatNeededForCommandMessage;
    public final ConfigEntry<String> playerCommandMessage;
    public final ConfigEntry<String> placeholderUsesVoicechat;
    public final ConfigEntry<String> placeholderNoVoicechat;
    public final ConfigEntry<String> placeholderVoicechatDisabled;

    public Translations(ConfigBuilder builder) {
        builder.header(
                "Simple Voice Chat translations",
                "This file contains all server-side translations for the Simple Voice Chat mod"
        );

        forceVoicechatKickMessage = builder.stringEntry(
                "force_voicechat_kick_message",
                "You need %s %s to play on this server",
                "The message a player gets when kicked for not having voice chat installed and the server has force_voicechat enabled",
                "The first parameter is the mod/plugin name and the second parameter is the mod/plugin version"
        );
        voicechatNotCompatibleMessage = builder.stringEntry(
                "voicechat_not_compatible_message",
                "Your voice chat version is not compatible with the servers version.\\nPlease install version %s of %s.",
                "The message a player gets when joining a server with an incompatible voice chat version",
                "The first parameter is the mod/plugin version and the second parameter is the mod/plugin name"
        );
        voicechatNeededForCommandMessage = builder.stringEntry(
                "voicechat_needed_for_command_message",
                "You need to have %s installed on your client to use this command",
                "The message a player gets when trying to execute a command that requires voice chat",
                "The first parameter is the mod/plugin name"
        );
        playerCommandMessage = builder.stringEntry(
                "player_command_message",
                "This command can only be executed as a player",
                "The message a player gets when trying to execute a command that can only be executed as a player"
        );
        placeholderUsesVoicechat = builder.stringEntry(
                "placeholder_api_uses_voicechat",
                "§a",
                "The Placeholder API string for 'prefix_uses_voicechat'"
        );
        placeholderNoVoicechat = builder.stringEntry(
                "placeholder_api_no_voicechat",
                "§c",
                "The Placeholder API string for 'prefix_no_voicechat'"
        );
        placeholderVoicechatDisabled = builder.stringEntry(
                "placeholder_api_disabled",
                "§c",
                "The Placeholder API string for 'prefix_disabled'"
        );
    }

}
