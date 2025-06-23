package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import de.maxhenkel.voicechat.gui.audiodevice.MicrophoneAudioDeviceList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class MicOnboardingScreen extends DeviceOnboardingScreen {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.microphone").setStyle(new Style().setBold(true));

    public MicOnboardingScreen(@Nullable GuiScreen previous) {
        super(TITLE, previous);
    }

    @Override
    public AudioDeviceList createAudioDeviceList(int width, int height, int top) {
        return new MicrophoneAudioDeviceList(width, height, top);
    }

    @Override
    public GuiScreen getNextScreen() {
        return new SpeakerOnboardingScreen(this);
    }

}
