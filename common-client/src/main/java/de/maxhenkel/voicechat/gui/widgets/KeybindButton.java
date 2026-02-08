package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
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
    public void onPress(InputWithModifiers inputWithModifiers) {
        listening = true;
        updateText();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        renderDefaultSprite(guiGraphics);
        renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent evt, boolean bl) {
        if (listening) {
            if (evt.button() == 0) {
                // Don't allow left click when accidentally clicking the button twice
                listening = false;
                updateText();
                return isMouseOver(evt.x(), evt.y());
            }
            keyMapping.setKey(InputConstants.Type.MOUSE.getOrCreate(evt.button()));
            KeyMapping.resetMapping();
            mc.options.save();
            listening = false;
            updateText();
            return true;
        }
        return super.mouseClicked(evt, bl);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (listening) {
            if (keyEvent.isEscape()) {
                keyMapping.setKey(InputConstants.UNKNOWN);
            } else {
                keyMapping.setKey(InputConstants.getKey(keyEvent));
            }
            KeyMapping.resetMapping();
            mc.options.save();
            listening = false;
            updateText();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        if (listening && keyEvent.isEscape()) {
            return true;
        }
        return super.keyReleased(keyEvent);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public boolean isListening() {
        return listening;
    }

    public void resetListening() {
        listening = false;
        updateText();
    }
}
