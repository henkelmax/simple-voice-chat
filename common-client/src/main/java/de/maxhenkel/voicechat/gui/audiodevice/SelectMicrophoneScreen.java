package de.maxhenkel.voicechat.gui.audiodevice;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class SelectMicrophoneScreen extends SelectDeviceScreen {

    public static final Component TITLE = Component.translatable("gui.voicechat.select_microphone.title");
    public static final Component NO_MICROPHONE = Component.translatable("message.voicechat.no_microphone").withStyle(ChatFormatting.GRAY);

    public SelectMicrophoneScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public Component getEmptyListComponent() {
        return NO_MICROPHONE;
    }

    @Override
    public AudioDeviceList createAudioDeviceList(int width, int height, int top) {
        return new MicrophoneAudioDeviceList(this, width, height, top);
    }

}
