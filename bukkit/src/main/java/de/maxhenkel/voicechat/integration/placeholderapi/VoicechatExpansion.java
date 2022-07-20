package de.maxhenkel.voicechat.integration.placeholderapi;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.Server;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class VoicechatExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return Voicechat.MODID;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", Voicechat.INSTANCE.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return Voicechat.INSTANCE.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return super.onPlaceholderRequest(player, params);
        }

        if (params.equalsIgnoreCase("prefix_uses_voicechat")) {
            return Voicechat.SERVER.isCompatible(player) ? Voicechat.translate("placeholder_api_prefix_uses_voicechat") : "";
        } else if (params.equalsIgnoreCase("prefix_no_voicechat")) {
            return !Voicechat.SERVER.isCompatible(player) ? Voicechat.translate("placeholder_api_prefix_no_voicechat") : "";
        } else if (params.equalsIgnoreCase("prefix_disabled")) {
            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                return "";
            }
            PlayerState state = server.getPlayerStateManager().getState(player.getUniqueId());
            if (state == null) {
                return "";
            }
            if (!state.isDisabled()) {
                return "";
            }
            return Voicechat.translate("placeholder_api_prefix_disabled");
        }

        return super.onPlaceholderRequest(player, params);
    }
}
