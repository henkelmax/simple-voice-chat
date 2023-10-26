package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;

public class Translations {

    public final ConfigEntry<String> forceVoicechatKickMessage;
    public final ConfigEntry<String> voicechatNotCompatibleMessage;
    public final ConfigEntry<String> voicechatNeededForCommandMessage;
    public final ConfigEntry<String> playerCommandMessage;
    public final ConfigEntry<String> placeholderVoicechatInstalled;
    public final ConfigEntry<String> placeholderVoicechatNotInstalled;
    public final ConfigEntry<String> placeholderVoicechatDisabled;

    public Translations(ConfigBuilder builder) {
        builder.header(
                "Simple Voice Chat translations",
                "This file contains all server-side translations for the Simple Voice Chat plugin"
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
