package de.maxhenkel.voicechat.integration;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import me.shedaniel.clothconfig2.gui.ClothConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

public class ClothConfig {

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = checkLoaded();
        }
        return loaded;
    }

    private static boolean checkLoaded() {
        if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            try {
                Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
                Voicechat.LOGGER.info("Using Cloth Config GUI");
                return true;
            } catch (Exception e) {
                Voicechat.LOGGER.warn("Failed to load Cloth Config: {}", e.getMessage());
            }
        }
        return false;
    }

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (isLoaded()) {
                if (client.screen instanceof ClothConfigScreen screen) {
                    if (screen.getSelectedCategory().equals(ClothConfigWrapper.OTHER_SETTINGS)) {
                        screen.selectedCategoryIndex = 0;
                        client.setScreen(new VoiceChatSettingsScreen(client.screen));
                    }
                }
            }
        });
    }

}
