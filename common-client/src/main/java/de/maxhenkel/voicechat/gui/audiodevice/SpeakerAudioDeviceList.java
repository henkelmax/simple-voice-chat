package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SpeakerAudioDeviceList extends AudioDeviceList {

    public static final Identifier SPEAKER_ICON = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker.png");
    public static final Component DEFAULT_SPEAKER = Component.translatable("message.voicechat.default_speaker");

    public SpeakerAudioDeviceList(int width, int height, int top) {
        super(width, height, top);
        defaultDeviceText = DEFAULT_SPEAKER;
        icon = SPEAKER_ICON;
        configEntry = VoicechatClient.CLIENT_CONFIG.speaker;
        setAudioDevices(SoundManager.getAllSpeakers());
    }

    @Override
    public AudioDeviceEntry createAudioDeviceEntry(String device, Component name, @Nullable Identifier icon, Supplier<Boolean> isSelected) {
        return new SpeakerAudioDeviceEntry(device, name, icon, isSelected);
    }

}
