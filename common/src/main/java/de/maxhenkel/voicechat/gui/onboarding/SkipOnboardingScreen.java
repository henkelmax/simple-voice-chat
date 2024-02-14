package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class SkipOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.skip.title", CommonCompatibilityManager.INSTANCE.getModName()).setStyle(new Style().setBold(true));
    private static final ITextComponent DESCRIPTION = new TextComponentTranslation("message.voicechat.onboarding.skip.description");
    private static final ITextComponent CONFIRM = new TextComponentTranslation("message.voicechat.onboarding.confirm");

    public SkipOnboardingScreen(@Nullable GuiScreen previous) {
        super(TITLE, previous);
    }

    @Override
    public void initGui() {
        super.initGui();

        addBackOrCancelButton(0);
        addPositiveButton(1, CONFIRM, button -> OnboardingManager.finishOnboarding());
    }

    @Override
    public GuiScreen getNextScreen() {
        return previous;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderTitle(TITLE);
        renderMultilineText(DESCRIPTION);
    }

}
