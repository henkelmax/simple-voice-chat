package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import de.maxhenkel.voicechat.gui.audiodevice.MicrophoneAudioDeviceList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class MicOnboardingScreen extends DeviceOnboardingScreen {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.microphone").withStyle(TextFormatting.BOLD);

    public MicOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    public AudioDeviceList createAudioDeviceList(int width, int height, int top) {
        return new MicrophoneAudioDeviceList(width, height, top);
    }

    @Override
    public Screen getNextScreen() {
        return new SpeakerOnboardingScreen(this);
    }

}
