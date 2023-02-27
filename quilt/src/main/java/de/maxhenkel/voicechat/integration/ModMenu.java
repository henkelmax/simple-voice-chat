package de.maxhenkel.voicechat.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.integration.clothconfig.ClothConfig;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (ClothConfig.isLoaded()) {
            return getClothConfigFactory();
        } else {
            return VoiceChatSettingsScreen::new;
        }
    }

    private ConfigScreenFactory<?> getClothConfigFactory() {
        return screen -> ClientCompatibilityManager.INSTANCE.getClothConfigIntegration().createConfigScreen(screen);
    }

}