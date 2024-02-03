package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class IntroductionOnboardingScreen extends OnboardingScreenBase {

    private static final Component TITLE = Component.translatable("message.voicechat.onboarding.introduction.title", CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD);
    private static final Component DESCRIPTION = Component.translatable("message.voicechat.onboarding.introduction.description");
    private static final Component SKIP = Component.translatable("message.voicechat.onboarding.introduction.skip");

    public IntroductionOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {
        super.init();

        Button skipButton = Button.builder(SKIP, button -> {
            OnboardingManager.finishOnboarding();
        }).bounds(guiLeft, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth, BUTTON_HEIGHT).build();
        addRenderableWidget(skipButton);

        addNextButton();
        addBackOrCancelButton();
    }

    @Override
    public Screen getNextScreen() {
        return new MicOnboardingScreen(this);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        renderTitle(guiGraphics, TITLE);
        renderMultilineText(guiGraphics, DESCRIPTION);
    }

}
