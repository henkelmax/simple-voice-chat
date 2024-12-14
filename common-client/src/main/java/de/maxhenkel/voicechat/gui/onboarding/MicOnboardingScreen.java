package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectMicrophoneScreen;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.List;

public class MicOnboardingScreen extends DeviceOnboardingScreen {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.microphone").setStyle(new Style().setBold(true));

    public MicOnboardingScreen(@Nullable GuiScreen previous) {
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
    public GuiScreen getNextScreen() {
        return new SpeakerOnboardingScreen(this);
    }

}
