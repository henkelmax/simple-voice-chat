package de.maxhenkel.voicechat.gui.audiodevice;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class SelectMicrophoneScreen extends SelectDeviceScreen {

    public static final ITextComponent TITLE = new TextComponentTranslation("gui.voicechat.select_microphone.title");
    public static final ITextComponent NO_MICROPHONE = new TextComponentTranslation("message.voicechat.no_microphone").setStyle(new Style().setColor(TextFormatting.GRAY));

    public SelectMicrophoneScreen(@Nullable GuiScreen parent) {
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
