package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import de.maxhenkel.voicechat.gui.audiodevice.SpeakerAudioDeviceList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class SpeakerOnboardingScreen extends DeviceOnboardingScreen {

    private static final Component TITLE = Component.translatable("message.voicechat.onboarding.speaker").withStyle(ChatFormatting.BOLD);

    public SpeakerOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    public AudioDeviceList createAudioDeviceList(int width, int height, int top) {
        return new SpeakerAudioDeviceList(width, height, top);
    }

    @Override
    public Screen getNextScreen() {
        return new MicOnboardingScreen(this);
    }

}
