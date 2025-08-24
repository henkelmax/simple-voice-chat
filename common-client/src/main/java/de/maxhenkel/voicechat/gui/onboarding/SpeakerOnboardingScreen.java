package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import de.maxhenkel.voicechat.gui.audiodevice.SpeakerAudioDeviceList;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class SpeakerOnboardingScreen extends DeviceOnboardingScreen {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.speaker").withStyle(TextFormatting.BOLD);

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
