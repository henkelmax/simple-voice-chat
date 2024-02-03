package de.maxhenkel.voicechat.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;

public class KeybindButton extends ButtonBase {

    private static final Minecraft mc = Minecraft.getMinecraft();

    protected KeyBinding keyMapping;
    @Nullable
    protected ITextComponent description;
    protected boolean listening;

    public KeybindButton(int id, KeyBinding mapping, int x, int y, int width, int height, @Nullable ITextComponent description) {
        super(id, x, y, width, height, "");
        this.keyMapping = mapping;
        this.description = description;
        updateText();
    }

    public KeybindButton(int id, KeyBinding mapping, int x, int y, int width, int height) {
        this(id, mapping, x, y, width, height, null);
    }

    protected void updateText() {
        ITextComponent text;
        if (listening) {
            text = new TextComponentString("> ").appendSibling(getText(keyMapping).setStyle(new Style().setColor(TextFormatting.BOLD).setColor(TextFormatting.UNDERLINE))).appendText(" <").setStyle(new Style().setColor(TextFormatting.YELLOW));
        } else {
            text = getText(keyMapping);
        }

        if (description != null) {
            text = new TextComponentString("").appendSibling(description).appendText(": ").appendSibling(text);
        }

        displayString = text.getFormattedText();
    }

    private static ITextComponent getText(KeyBinding keyMapping) {
        return new TextComponentString(keyMapping.getDisplayName());
    }

    public boolean isHovered() {
        return hovered;
    }

    @Override
    public void onPress() {
        listening = true;
        updateText();
    }

    public boolean mousePressed(int button) {
        if (listening) {
            setKeyBind(button - 100);
            listening = false;
            updateText();
            return true;
        }
        return false;
    }

    public boolean keyPressed(int key) {
        if (listening) {
            if (key == Keyboard.KEY_ESCAPE) {
                setKeyBind(Keyboard.KEY_NONE);
            } else {
                setKeyBind(key);
            }
            listening = false;
            updateText();
            return true;
        }
        return false;
    }

    protected void setKeyBind(int key) {
        mc.gameSettings.setOptionKeyBinding(keyMapping, key);
        KeyBinding.resetKeyBindingArrayAndHash();
    }

    public boolean isListening() {
        return listening;
    }
}
