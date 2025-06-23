package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class SpeakerAudioDeviceList extends AudioDeviceList {

    public static final ResourceLocation SPEAKER_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker.png");
    public static final Component DEFAULT_SPEAKER = new TranslatableComponent("message.voicechat.default_speaker");

    public SpeakerAudioDeviceList(int width, int height, int top) {
        super(width, height, top);
        defaultDeviceText = DEFAULT_SPEAKER;
        icon = SPEAKER_ICON;
        configEntry = VoicechatClient.CLIENT_CONFIG.speaker;
        setAudioDevices(SoundManager.getAllSpeakers());
    }

}
