package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.natives.SpeexManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Consumer;

public class AgcButton extends BooleanConfigButton {

    private static final Component AUTO = new TranslatableComponent("message.voicechat.gain.auto");
    private static final Component MANUAL = new TranslatableComponent("message.voicechat.gain.manual").withStyle(ChatFormatting.RED);
    private static final Component MANUAL_WARNING = new TranslatableComponent("message.voicechat.gain.manual.warning").withStyle(ChatFormatting.RED);

    private final Consumer<Boolean> onChange;
    private final Screen parent;

    public AgcButton(Screen parent, int x, int y, int width, int height, Consumer<Boolean> onChange) {
        super(x, y, width, height, VoicechatClient.CLIENT_CONFIG.agc, enabled -> {
            TranslatableComponent translatable = new TranslatableComponent("message.voicechat.gain", enabled ? AUTO : MANUAL);
            if (!enabled) {
                translatable.withStyle(ChatFormatting.RED);
            }
            return translatable;
        });
        this.parent = parent;
        this.onChange = onChange;
        if (!SpeexManager.canUseAgc()) {
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

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        super.renderButton(poseStack, i, j, f);
        if (isHoveredOrFocused()) {
            renderToolTip(poseStack, i, j);
        }
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int i, int j) {
        super.renderToolTip(poseStack, i, j);
        if (!entry.get()) {
            parent.renderTooltip(poseStack, Minecraft.getInstance().font.split(MANUAL_WARNING, 200), i, j);
        }
    }

}
