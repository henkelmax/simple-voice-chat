package de.maxhenkel.voicechat.integration.placeholderapi;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.Group;
import de.maxhenkel.voicechat.voice.server.Server;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Locale;

public class VoicechatExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return Voicechat.MODID;
    }

    @Override
    public String getAuthor() {
        return String.join(", ", Voicechat.INSTANCE.getDescription().getAuthors());
    }

    @Override
    public String getVersion() {
        return Voicechat.INSTANCE.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return super.onPlaceholderRequest(player, params);
        }

        boolean installed = Voicechat.SERVER.isCompatible(player.getUniqueId());
        Server server = Voicechat.SERVER.getServer();
        PlayerState state = server == null ? null : server.getPlayerStateManager().getState(player.getUniqueId());
        if (state == null) {
            return params.equalsIgnoreCase("installed") ? bool(installed) : "";
        }

        switch (params.toLowerCase(Locale.ROOT)) {
            case "installed":
                return bool(installed);
            case "disconnected":
                return bool(state.isDisconnected());
            case "disabled":
                return bool(state.isDisabledOrForceDisabled());
            case "force_disabled":
                return bool(state.isForceDisabled());
            case "muted":
                return bool(state.isMutedOrForceMuted());
            case "force_muted":
                return bool(state.isForceMuted());
            case "group":
                Group group = state.hasGroup() ? server.getGroupManager().getGroup(state.getGroup()) : null;
                return group == null ? "" : group.getName();
            case "status":
                return status(installed, state);
        }

        return super.onPlaceholderRequest(player, params);
    }

    private static String status(boolean installed, PlayerState state) {
        if (!installed) {
            return "not_installed";
        }
        if (state.isDisconnected()) {
            return "disconnected";
        }
        if (state.isForceDisabled()) {
            return "force_disabled";
        }
        if (state.isDisabled()) {
            return "disabled";
        }
        if (state.isForceMuted()) {
            return "force_muted";
        }
        if (state.isMuted()) {
            return "muted";
        }
        return "active";
    }

    private static String bool(boolean value) {
        return value ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

}
