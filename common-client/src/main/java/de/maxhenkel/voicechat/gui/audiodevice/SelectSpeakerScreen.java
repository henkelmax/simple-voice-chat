package de.maxhenkel.voicechat.gui.audiodevice;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public class SelectSpeakerScreen extends SelectDeviceScreen {

    public static final ITextComponent TITLE = new TextComponentTranslation("gui.voicechat.select_speaker.title");
    public static final ITextComponent NO_SPEAKER = new TextComponentTranslation("message.voicechat.no_speaker").setStyle(new Style().setColor(TextFormatting.GRAY));

    public SelectSpeakerScreen(@Nullable GuiScreen parent) {
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
