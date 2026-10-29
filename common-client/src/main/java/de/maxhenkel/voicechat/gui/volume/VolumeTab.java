package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class VolumeTab {

    public static final VolumeTab PLAYERS = new VolumeTab(Component.translatable("message.voicechat.volume_tab.players"), VolumeTab::createPlayerEntries);
    public static final VolumeTab OTHER = new VolumeTab(Component.translatable("message.voicechat.volume_tab.other"), VolumeTab::createOtherEntries);

    private final Component title;
    private final Function<AdjustVolumesScreen, List<VolumeEntry>> entryFactory;

    public VolumeTab(Component title, Function<AdjustVolumesScreen, List<VolumeEntry>> entryFactory) {
        this.title = title;
        this.entryFactory = entryFactory;
    }

    public Component getTitle() {
        return title;
    }

    public List<VolumeEntry> createEntries(AdjustVolumesScreen screen) {
        return entryFactory.apply(screen);
    }

    private static List<VolumeEntry> createPlayerEntries(AdjustVolumesScreen screen) {
        List<VolumeEntry> entries = new ArrayList<>();
        Collection<PlayerState> onlinePlayers = ClientManager.getPlayerStateManager().getPlayerStates(false);

        for (PlayerState state : onlinePlayers) {
            entries.add(new PlayerVolumeEntry(state, screen));
        }

        if (VoicechatClient.CLIENT_CONFIG.offlinePlayerVolumeAdjustment.get()) {
            addOfflinePlayers(entries, onlinePlayers, screen);
        }
        return entries;
    }

    private static void addOfflinePlayers(List<VolumeEntry> entries, Collection<PlayerState> onlinePlayers, AdjustVolumesScreen screen) {
        for (UUID uuid : VoicechatClient.PLAYER_VOLUME_CONFIG.getVolumes().keySet()) {
            if (uuid.equals(Util.NIL_UUID)) {
                continue;
            }
            if (onlinePlayers.stream().anyMatch(state -> uuid.equals(state.getUuid()))) {
                continue;
            }

            String name = VoicechatClient.USERNAME_CACHE.getUsername(uuid);

            if (name == null) {
                continue;
            }

            entries.add(new PlayerVolumeEntry(new PlayerState(uuid, name, false, true), screen));
        }
    }

    private static List<VolumeEntry> createOtherEntries(AdjustVolumesScreen screen) {
        List<VolumeEntry> entries = new ArrayList<>();
        entries.add(new PlayerVolumeEntry(null, screen));
        for (VolumeCategoryImpl category : ClientManager.getCategoryManager().getCategories()) {
            entries.add(new CategoryVolumeEntry(category, screen));
        }
        return entries;
    }

}
