package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.widgets.ButtonBase;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class IntroductionOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.introduction.title", CommonCompatibilityManager.INSTANCE.getModName()).setStyle(new Style().setBold(true));
    private static final ITextComponent DESCRIPTION = new TextComponentTranslation("message.voicechat.onboarding.introduction.description");
    private static final ITextComponent SKIP = new TextComponentTranslation("message.voicechat.onboarding.introduction.skip");

    public IntroductionOnboardingScreen(@Nullable GuiScreen previous) {
        super(TITLE, previous);
    }

    @Override
    public void initGui() {
        super.initGui();

        ButtonBase skipButton = new ButtonBase(0, guiLeft, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth, BUTTON_HEIGHT, SKIP) {
            @Override
            public void onPress() {
                OnboardingManager.finishOnboarding();
            }
        };
        addButton(skipButton);

        addNextButton(1);
        addBackOrCancelButton(2);
    }

    @Override
    public GuiScreen getNextScreen() {
        return new MicOnboardingScreen(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderTitle(TITLE);
        renderMultilineText(DESCRIPTION);
    }

}
