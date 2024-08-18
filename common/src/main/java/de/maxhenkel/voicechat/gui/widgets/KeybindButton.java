package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public class KeybindButton extends AbstractButton {

    private static final Minecraft mc = Minecraft.getInstance();

    protected KeyMapping keyMapping;
    @Nullable
    protected Component description;
    protected boolean listening;

    public KeybindButton(KeyMapping mapping, int x, int y, int width, int height, @Nullable Component description) {
        super(x, y, width, height, Component.empty());
        this.keyMapping = mapping;
        this.description = description;
        updateText();
    }

    public KeybindButton(KeyMapping mapping, int x, int y, int width, int height) {
        this(mapping, x, y, width, height, null);
    }

    protected void updateText() {
        MutableComponent text;
        if (listening) {
            text = Component.literal("> ").append(getText(keyMapping).copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW);
        } else {
            text = getText(keyMapping).copy();
        }

        if (description != null) {
            text = description.copy().append(": ").append(text);
        }

        setMessage(text);
    }

    private static Component getText(KeyMapping keyMapping) {
        return keyMapping.getTranslatedKeyMessage();
    }

    @Override
    public void onPress() {
        listening = true;
        updateText();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (listening) {
            keyMapping.setKey(InputConstants.Type.MOUSE.getOrCreate(button));
            mc.options.save();
            listening = false;
            updateText();
            return true;
        }
        return super.mouseClicked(x, y, button);

    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (listening) {
            if (key == InputConstants.KEY_ESCAPE) {
                keyMapping.setKey(InputConstants.UNKNOWN);
            } else {
                keyMapping.setKey(InputConstants.getKey(key, scanCode));
            }
            mc.options.save();
            listening = false;
            updateText();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (listening && key == InputConstants.KEY_ESCAPE) {
            return true;
        }
        return super.keyReleased(key, scanCode, modifiers);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public boolean isListening() {
        return listening;
    }
}
