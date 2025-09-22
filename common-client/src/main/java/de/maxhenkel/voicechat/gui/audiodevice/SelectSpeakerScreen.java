package de.maxhenkel.voicechat.gui.audiodevice;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class SelectSpeakerScreen extends SelectDeviceScreen {

    public static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.select_speaker.title");
    public static final ITextComponent NO_SPEAKER = new TranslationTextComponent("message.voicechat.no_speaker").withStyle(TextFormatting.GRAY);

    public SelectSpeakerScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public ITextComponent getEmptyListComponent() {
        return NO_SPEAKER;
    }

    @Override
    public AudioDeviceList createAudioDeviceList(int width, int height, int top) {
        return new SpeakerAudioDeviceList(width, height, top);
    }

}
