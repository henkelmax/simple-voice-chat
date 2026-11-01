package de.maxhenkel.voicechat.testaddon;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.PlayerForcedStateEvent;
import de.maxhenkel.voicechat.api.events.PlayerVisibilityEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

import javax.annotation.Nullable;
import java.util.UUID;

public class TestVoicechatPlugin implements VoicechatPlugin {

    private static VoicechatServerApi serverApi;

    @Override
    public String getPluginId() {
        return "voicechat_test_addon";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, event -> serverApi = event.getVoicechat());
        registration.registerEvent(PlayerForcedStateEvent.class, this::onForcedState);
        registration.registerEvent(PlayerVisibilityEvent.class, this::onVisibility);
    }

    public static boolean registerVolumeCategory(String id, String name, @Nullable String tab, @Nullable String tabName) {
        if (serverApi == null) {
            return false;
        }
        serverApi.registerVolumeCategory(serverApi.volumeCategoryBuilder()
                .setId(id)
                .setName(name)
                .setTab(tab)
                .setTabName(tabName)
                .build());
        return true;
    }

    public static void evaluateForcedState(UUID playerUuid) {
        if (serverApi == null) {
            return;
        }
        VoicechatConnection connection = serverApi.getConnectionOf(playerUuid);
        if (connection != null) {
            connection.evaluateForcedState();
        }
    }

    public static void evaluateVisibility(UUID playerUuid) {
        if (serverApi == null) {
            return;
        }
        VoicechatConnection connection = serverApi.getConnectionOf(playerUuid);
        if (connection != null) {
            connection.evaluateVisibility();
        }
    }

    private void onForcedState(PlayerForcedStateEvent event) {
        UUID uuid = event.getConnection().getPlayer().getUuid();
        if (TestAddon.FORCE_MUTED.contains(uuid)) {
            event.forceMute();
        }
        if (TestAddon.FORCE_DISABLED.contains(uuid)) {
            event.forceDisable();
        }
    }

    private void onVisibility(PlayerVisibilityEvent event) {
        if (TestAddon.HIDDEN.contains(event.getPlayer().getUuid())) {
            event.hide();
        }
    }

}
