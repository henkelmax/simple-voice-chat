package de.maxhenkel.voicechat.gui.audiodevice;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class SelectSpeakerScreen extends SelectDeviceScreen {

    public static final Component TITLE = Component.translatable("gui.voicechat.select_speaker.title");
    public static final Component NO_SPEAKER = Component.translatable("message.voicechat.no_speaker").withStyle(ChatFormatting.GRAY);

    public SelectSpeakerScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public Component getEmptyListComponent() {
        return NO_SPEAKER;
    }

    @Override
    public AudioDeviceList createAudioDeviceList(int width, int height, int top) {
        return new SpeakerAudioDeviceList(width, height, top);
    }

}
