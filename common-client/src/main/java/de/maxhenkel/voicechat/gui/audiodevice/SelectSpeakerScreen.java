package de.maxhenkel.voicechat.gui.audiodevice;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class SelectSpeakerScreen extends SelectDeviceScreen {

    public static final Component TITLE = new TranslatableComponent("gui.voicechat.select_speaker.title");
    public static final Component NO_SPEAKER = new TranslatableComponent("message.voicechat.no_speaker").withStyle(ChatFormatting.GRAY);

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
