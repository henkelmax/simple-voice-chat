package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectSpeakerScreen;
import de.maxhenkel.voicechat.voice.client.AudioChannelConfig;
import de.maxhenkel.voicechat.voice.client.DataLines;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.List;

public class SpeakerOnboardingScreen extends DeviceOnboardingScreen {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.speaker").setStyle(new Style().setBold(true));

    public SpeakerOnboardingScreen(@Nullable GuiScreen previous) {
        super(TITLE, previous);
    }

    @Override
    public List<String> getNames() {
        return DataLines.getSpeakerNames(AudioChannelConfig.STEREO_FORMAT);
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
    public GuiScreen getNextScreen() {
        return new ActivationOnboardingScreen(this);
    }

}
