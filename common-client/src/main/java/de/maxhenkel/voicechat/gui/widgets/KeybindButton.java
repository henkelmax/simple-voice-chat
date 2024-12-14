package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;

public class KeybindButton extends AbstractButton {

    private static final Minecraft mc = Minecraft.getInstance();

    protected KeyMapping keyMapping;
    @Nullable
    protected Component description;
    protected boolean listening;

    public KeybindButton(KeyMapping mapping, int x, int y, int width, int height, @Nullable Component description) {
        super(x, y, width, height, new TextComponent(""));
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
            text = new TextComponent("> ").append(getText(keyMapping).copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)).append(" <").withStyle(ChatFormatting.YELLOW);
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

    public boolean isHovered() {
        return isHovered;
    }

    @Override
    public void onPress() {
        listening = true;
        updateText();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (listening) {
            mc.options.setKey(keyMapping, InputConstants.Type.MOUSE.getOrCreate(button));
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
                mc.options.setKey(keyMapping, InputConstants.UNKNOWN);
            } else {
                mc.options.setKey(keyMapping, InputConstants.getKey(key, scanCode));
            }
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
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    public boolean isListening() {
        return listening;
    }
}
