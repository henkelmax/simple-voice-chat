package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class IntroductionOnboardingScreen extends OnboardingScreenBase {

    private static final Component TITLE = new TranslatableComponent("message.voicechat.onboarding.introduction.title", CommonCompatibilityManager.INSTANCE.getModName()).withStyle(ChatFormatting.BOLD);
    private static final Component DESCRIPTION = new TranslatableComponent("message.voicechat.onboarding.introduction.description");
    private static final Component SKIP = new TranslatableComponent("message.voicechat.onboarding.introduction.skip");

    public IntroductionOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {
        super.init();

        Button skipButton = new Button(guiLeft, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth, BUTTON_HEIGHT, SKIP, button -> {
            minecraft.setScreen(new SkipOnboardingScreen(IntroductionOnboardingScreen.this));
        });
        addRenderableWidget(skipButton);

        addNextButton();
        addBackOrCancelButton();
    }

    @Override
    public Screen getNextScreen() {
        return new MicOnboardingScreen(this);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        renderTitle(poseStack, TITLE);
        renderMultilineText(poseStack, DESCRIPTION);
    }

}
