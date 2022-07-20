package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class SelectSpeakerScreen extends SelectDeviceScreen {

    protected static final ResourceLocation SPEAKER_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker.png");
    protected static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.select_speaker.title");
    protected static final ITextComponent NO_SPEAKER = new TranslationTextComponent("message.voicechat.no_speaker").withStyle(TextFormatting.GRAY);

    public SelectSpeakerScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public List<String> getDevices() {
        return SoundManager.getAllSpeakers();
    }

    @Override
    public String getSelectedDevice() {
        return VoicechatClient.CLIENT_CONFIG.speaker.get();
    }

    @Override
    public ResourceLocation getIcon(String device) {
        return SPEAKER_ICON;
    }

    @Override
    public ITextComponent getEmptyListComponent() {
        return NO_SPEAKER;
    }

    @Override
    public String getVisibleName(String device) {
        return SoundManager.cleanDeviceName(device);
    }

    @Override
    public void onSelect(String device) {
        VoicechatClient.CLIENT_CONFIG.speaker.set(device).save();
        ClientVoicechat client = ClientManager.getClient();
        if (client != null) {
            client.reloadAudio();
        }
    }
}
