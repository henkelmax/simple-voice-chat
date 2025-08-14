package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.AutomaticGainControl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Consumer;

public class AgcButton extends BooleanConfigButton {

    private static final Component AUTO = new TranslatableComponent("message.voicechat.gain.auto");
    private static final Component MANUAL = new TranslatableComponent("message.voicechat.gain.manual");

    private final Consumer<Boolean> onChange;

    public AgcButton(int x, int y, int width, int height, Consumer<Boolean> onChange) {
        super(x, y, width, height, VoicechatClient.CLIENT_CONFIG.agc, enabled -> {
            return new TranslatableComponent("message.voicechat.gain", enabled ? AUTO : MANUAL);
        });
        this.onChange = onChange;
        if (!AutomaticGainControl.canUseAgc()) {
            active = false;
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
