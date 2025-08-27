package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.natives.SpeexManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Consumer;

public class AgcButton extends BooleanConfigButton {

    private static final ITextComponent AUTO = new TextComponentTranslation("message.voicechat.gain.auto");
    private static final ITextComponent MANUAL = new TextComponentTranslation("message.voicechat.gain.manual");

    private final Consumer<Boolean> onChange;

    public AgcButton(int id, int x, int y, int width, int height, Consumer<Boolean> onChange) {
        super(id, x, y, width, height, VoicechatClient.CLIENT_CONFIG.agc, enabled -> {
            return new TextComponentTranslation("message.voicechat.gain", enabled ? AUTO : MANUAL);
        });
        this.onChange = onChange;
        if (!SpeexManager.canUseAgc()) {
            enabled = false;
            onChange.accept(false);
        } else {
            onChange.accept(entry.get());
        }
    }

    @Override
    public void onPress() {
        super.onPress();
        onChange.accept(entry.get());
    }

}
