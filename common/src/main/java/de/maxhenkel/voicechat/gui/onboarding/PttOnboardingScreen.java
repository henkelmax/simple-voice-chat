package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.widgets.KeybindButton;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.io.IOException;

public class PttOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.ptt.title").setStyle(new Style().setBold(true));
    private static final ITextComponent DESCRIPTION = new TextComponentTranslation("message.voicechat.onboarding.ptt.description");
    private static final ITextComponent BUTTON_DESCRIPTION = new TextComponentTranslation("message.voicechat.onboarding.ptt.button_description");

    protected KeybindButton keybindButton;

    protected int keybindButtonPos;

    public PttOnboardingScreen(@Nullable GuiScreen previous) {
        super(TITLE, previous);
    }

    @Override
    public void initGui() {
        super.initGui();

        keybindButtonPos = guiTop + contentHeight - BUTTON_HEIGHT * 3 - PADDING * 2 - 40;
        keybindButton = new KeybindButton(0, KeyEvents.KEY_PTT, guiLeft + 40, keybindButtonPos, contentWidth - 40 * 2, BUTTON_HEIGHT);
        addButton(keybindButton);

        addNextButton(1);
        addBackOrCancelButton(2);
    }

    @Override
    public GuiScreen getNextScreen() {
        return new FinalOnboardingScreen(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderTitle(TITLE);
        renderMultilineText(DESCRIPTION);
        fontRenderer.drawStringWithShadow(BUTTON_DESCRIPTION.getFormattedText(), width / 2 - fontRenderer.getStringWidth(BUTTON_DESCRIPTION.getUnformattedComponentText()) / 2, keybindButtonPos - fontRenderer.FONT_HEIGHT - PADDING, TEXT_COLOR);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keybindButton.keyPressed(keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (keybindButton.mousePressed(mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
