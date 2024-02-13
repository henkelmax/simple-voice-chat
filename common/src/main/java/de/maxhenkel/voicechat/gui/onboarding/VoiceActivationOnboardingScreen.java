package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.widgets.DenoiserButton;
import de.maxhenkel.voicechat.gui.widgets.MicAmplificationSlider;
import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import de.maxhenkel.voicechat.gui.widgets.VoiceActivationSlider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class VoiceActivationOnboardingScreen extends OnboardingScreenBase {

    private static final Component TITLE = new TranslatableComponent("message.voicechat.onboarding.voice.title").withStyle(ChatFormatting.BOLD);
    private static final Component DESCRIPTION = new TranslatableComponent("message.voicechat.onboarding.voice.description");

    protected VoiceActivationSlider slider;
    protected MicTestButton micTestButton;

    public VoiceActivationOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {
        super.init();

        int bottom = guiTop + contentHeight - PADDING * 3 - BUTTON_HEIGHT * 2;
        int space = BUTTON_HEIGHT + SMALL_PADDING;

        addRenderableWidget(new MicAmplificationSlider(guiLeft, bottom - space * 2, contentWidth, BUTTON_HEIGHT));
        addRenderableWidget(new DenoiserButton(guiLeft, bottom - space, contentWidth, BUTTON_HEIGHT));

        slider = new VoiceActivationSlider(guiLeft + 20 + SMALL_PADDING, bottom, contentWidth - 20 - SMALL_PADDING, BUTTON_HEIGHT);
        micTestButton = new MicTestButton(guiLeft, bottom, slider);
        addRenderableWidget(micTestButton);
        addRenderableWidget(slider);

        addNextButton();
        addBackOrCancelButton();
    }

    @Override
    public Screen getNextScreen() {
        return new FinalOnboardingScreen(this);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);

        renderTitle(poseStack, TITLE);
        renderMultilineText(poseStack, DESCRIPTION);

        Component sliderTooltip = slider.getHoverText();
        if (slider.isHovered() && sliderTooltip != null) {
            renderTooltip(poseStack, sliderTooltip, mouseX, mouseY);
        } else if (micTestButton.isHovered()) {
            micTestButton.onTooltip(micTestButton, poseStack, mouseX, mouseY);
        }
    }
}
