package de.maxhenkel.voicechat.gui.audiodevice;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class SelectMicrophoneScreen extends SelectDeviceScreen {

    public static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.select_microphone.title");
    public static final ITextComponent NO_MICROPHONE = new TranslationTextComponent("message.voicechat.no_microphone").withStyle(TextFormatting.GRAY);

    public SelectMicrophoneScreen(@Nullable Screen parent) {
        super(TITLE, parent);
    }

    @Override
    public ITextComponent getEmptyListComponent() {
        return NO_MICROPHONE;
    }

    @Override
    public AudioDeviceList createAudioDeviceList(int width, int height, int top) {
        return new MicrophoneAudioDeviceList(this, width, height, top);
    }

}
