package de.maxhenkel.voicechat.integration.placeholderapi;

import de.maxhenkel.voicechat.BukkitUtils;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatPaperPlugin;
import de.maxhenkel.voicechat.config.PaperTranslations;
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
        return String.join(", ", VoicechatPaperPlugin.INSTANCE.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return VoicechatPaperPlugin.INSTANCE.getPluginMeta().getVersion();
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

        if (params.equalsIgnoreCase("installed")) {
            return Voicechat.SERVER.isCompatible(BukkitUtils.getPlayer(player)) ? ((PaperTranslations) Voicechat.TRANSLATIONS).placeholderVoicechatInstalled.get() : "";
        } else if (params.equalsIgnoreCase("not_installed")) {
            return !Voicechat.SERVER.isCompatible(BukkitUtils.getPlayer(player)) ? ((PaperTranslations) Voicechat.TRANSLATIONS).placeholderVoicechatNotInstalled.get() : "";
        } else if (params.equalsIgnoreCase("disabled")) {
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
            return ((PaperTranslations) Voicechat.TRANSLATIONS).placeholderVoicechatDisabled.get();
        }

        return super.onPlaceholderRequest(player, params);
    }
}
