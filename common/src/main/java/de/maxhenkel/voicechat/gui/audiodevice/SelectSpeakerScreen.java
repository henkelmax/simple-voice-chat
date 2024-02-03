package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.AudioChannelConfig;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.DataLines;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;

public class SelectSpeakerScreen extends SelectDeviceScreen {

    public static final ResourceLocation SPEAKER_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker.png");
    public static final ITextComponent TITLE = new TextComponentTranslation("gui.voicechat.select_speaker.title");
    public static final ITextComponent NO_SPEAKER = new TextComponentTranslation("message.voicechat.no_speaker").setStyle(new Style().setColor(TextFormatting.GRAY));

    public SelectSpeakerScreen(@Nullable GuiScreen parent) {
        super(TITLE, parent);
    }

    @Override
    public List<String> getDevices() {
        return DataLines.getSpeakerNames(AudioChannelConfig.STEREO_FORMAT);
        // return SoundManager.getAllSpeakers();
    }

    @Override
    public ResourceLocation getIcon() {
        return SPEAKER_ICON;
    }

    @Override
    public ITextComponent getEmptyListComponent() {
        return NO_SPEAKER;
    }

    @Override
    public ConfigEntry<String> getConfigEntry() {
        return VoicechatClient.CLIENT_CONFIG.speaker;
    }

}
