package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectMicrophoneScreen;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class MicOnboardingScreen extends DeviceOnboardingScreen {

    private static final Component TITLE = new TranslatableComponent("message.voicechat.onboarding.microphone").withStyle(ChatFormatting.BOLD);

    public MicOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    public List<String> getNames() {
        return MicrophoneManager.deviceNames();
    }

    @Override
    public ResourceLocation getIcon() {
        return SelectMicrophoneScreen.MICROPHONE_ICON;
    }

    @Override
    public ConfigEntry<String> getConfigEntry() {
        return VoicechatClient.CLIENT_CONFIG.microphone;
    }

    @Override
    public Screen getNextScreen() {
        return new SpeakerOnboardingScreen(this);
    }

}
