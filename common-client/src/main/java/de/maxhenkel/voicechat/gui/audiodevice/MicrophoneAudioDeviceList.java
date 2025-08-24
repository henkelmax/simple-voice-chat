package de.maxhenkel.voicechat.gui.audiodevice;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MicrophoneAudioDeviceList extends AudioDeviceList {

    public static final ResourceLocation MICROPHONE_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/microphone.png");
    public static final Component DEFAULT_MICROPHONE = Component.translatable("message.voicechat.default_microphone");

    public MicrophoneAudioDeviceList(int width, int height, int top) {
        super(width, height, top);
        defaultDeviceText = DEFAULT_MICROPHONE;
        icon = MICROPHONE_ICON;
        configEntry = VoicechatClient.CLIENT_CONFIG.microphone;
        setAudioDevices(MicrophoneManager.deviceNames());
    }

    @Override
    public AudioDeviceEntry createAudioDeviceEntry(String device, Component name, @Nullable ResourceLocation icon, Supplier<Boolean> isSelected) {
        return new AudioDeviceEntry(device, name, icon, isSelected);
    }

}
