package de.maxhenkel.voicechat.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;

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
        return ClothConfigWrapper::createConfigScreen;
    }

}
