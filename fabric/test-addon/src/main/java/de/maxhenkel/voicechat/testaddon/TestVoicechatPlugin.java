package de.maxhenkel.voicechat.testaddon;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.PlayerForcedStateEvent;

import java.util.UUID;

public class TestVoicechatPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return "voicechat_test_addon";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(PlayerForcedStateEvent.class, this::onForcedState);
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

}
