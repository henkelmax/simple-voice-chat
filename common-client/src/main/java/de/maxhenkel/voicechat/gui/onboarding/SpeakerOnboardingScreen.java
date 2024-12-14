package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectSpeakerScreen;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class SpeakerOnboardingScreen extends DeviceOnboardingScreen {

    private static final Component TITLE = new TranslatableComponent("message.voicechat.onboarding.speaker").withStyle(ChatFormatting.BOLD);

    public SpeakerOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    public List<String> getNames() {
        return SoundManager.getAllSpeakers();
    }

    @Override
    public ResourceLocation getIcon() {
        return SelectSpeakerScreen.SPEAKER_ICON;
    }

    @Override
    public ConfigEntry<String> getConfigEntry() {
        return VoicechatClient.CLIENT_CONFIG.speaker;
    }

    @Override
    public Screen getNextScreen() {
        return new ActivationOnboardingScreen(this);
    }

}
