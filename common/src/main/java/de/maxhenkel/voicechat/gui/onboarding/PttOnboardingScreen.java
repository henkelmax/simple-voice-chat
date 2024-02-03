package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.gui.widgets.KeybindButton;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class PttOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.ptt.title").withStyle(TextFormatting.BOLD);
    private static final ITextComponent DESCRIPTION = new TranslationTextComponent("message.voicechat.onboarding.ptt.description");
    private static final ITextComponent BUTTON_DESCRIPTION = new TranslationTextComponent("message.voicechat.onboarding.ptt.button_description");

    protected KeybindButton keybindButton;

    protected int keybindButtonPos;

    public PttOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {
        super.init();

        keybindButtonPos = guiTop + contentHeight - BUTTON_HEIGHT * 3 - PADDING * 2 - 40;
        keybindButton = new KeybindButton(KeyEvents.KEY_PTT, guiLeft + 40, keybindButtonPos, contentWidth - 40 * 2, BUTTON_HEIGHT);
        addButton(keybindButton);

        addNextButton();
        addBackOrCancelButton();
    }

    @Override
    public Screen getNextScreen() {
        return new FinalOnboardingScreen(this);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTitle(stack, TITLE);
        renderMultilineText(stack, DESCRIPTION);
        font.drawShadow(stack, BUTTON_DESCRIPTION.getVisualOrderText(), width / 2 - font.width(BUTTON_DESCRIPTION) / 2, keybindButtonPos - font.lineHeight - PADDING, TEXT_COLOR);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (keybindButton.isListening()) {
            return false;
        }
        return super.shouldCloseOnEsc();
    }
}
