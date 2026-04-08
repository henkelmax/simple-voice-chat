package de.maxhenkel.voicechat.integration.clothconfig;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;

public class ClothConfig {

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = checkLoaded();
        }
        return loaded;
    }

    private static boolean checkLoaded() {
        if (CommonCompatibilityManager.INSTANCE.isModLoaded("cloth-config") || CommonCompatibilityManager.INSTANCE.isModLoaded("cloth-config2") || CommonCompatibilityManager.INSTANCE.isModLoaded("cloth_config")) {
            try {
                Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
                Voicechat.LOGGER.info("Using Cloth Config GUI");
                return true;
            } catch (Exception e) {
                Voicechat.LOGGER.warn("Failed to load Cloth Config", e);
            }
        }
        return false;
    }

    public static void init() {
        if (isLoaded()) {
            ClientCompatibilityManager.INSTANCE.onClientTick(ClothConfig::onTick);
        }
    }

    private static void onTick() {
        if (isLoaded()) {
            ClothConfigIntegration.onTick();
        }
    }

}
