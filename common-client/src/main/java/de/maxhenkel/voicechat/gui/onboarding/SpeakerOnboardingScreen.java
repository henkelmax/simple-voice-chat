package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.audiodevice.AudioDeviceList;
import de.maxhenkel.voicechat.gui.audiodevice.SpeakerAudioDeviceList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class SpeakerOnboardingScreen extends DeviceOnboardingScreen {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.speaker").setStyle(new Style().setBold(true));

    public SpeakerOnboardingScreen(@Nullable GuiScreen previous) {
        super(TITLE, previous);
    }

    @Override
    public AudioDeviceList createAudioDeviceList(int width, int height, int top) {
        return new SpeakerAudioDeviceList(width, height, top);
    }

    @Override
    public GuiScreen getNextScreen() {
        return new ActivationOnboardingScreen(this);
    }

}
