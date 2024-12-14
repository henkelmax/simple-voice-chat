package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class SkipOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.skip.title").withStyle(TextFormatting.BOLD);
    private static final ITextComponent DESCRIPTION = new TranslationTextComponent("message.voicechat.onboarding.skip.description");
    private static final ITextComponent CONFIRM = new TranslationTextComponent("message.voicechat.onboarding.confirm");

    public SkipOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {
        super.init();

        addBackOrCancelButton();
        addPositiveButton(CONFIRM, button -> OnboardingManager.finishOnboarding());
    }

    @Override
    public Screen getNextScreen() {
        return previous;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTitle(stack, TITLE);
        renderMultilineText(stack, DESCRIPTION);
    }

}
