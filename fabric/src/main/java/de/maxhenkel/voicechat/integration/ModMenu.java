package de.maxhenkel.voicechat.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return VoiceChatSettingsScreen::new;
    }

}
