package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.widgets.DenoiserButton;
import de.maxhenkel.voicechat.gui.widgets.MicAmplificationSlider;
import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import de.maxhenkel.voicechat.gui.widgets.VoiceActivationSlider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class VoiceActivationOnboardingScreen extends OnboardingScreenBase {

    private static final Component TITLE = Component.translatable("message.voicechat.onboarding.voice.title").withStyle(ChatFormatting.BOLD);
    private static final Component DESCRIPTION = Component.translatable("message.voicechat.onboarding.voice.description");

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

        slider = new VoiceActivationSlider(guiLeft + 30 + SMALL_PADDING, bottom, contentWidth - 30 - SMALL_PADDING, BUTTON_HEIGHT);
        micTestButton = new MicTestButton(guiLeft, bottom, 30, BUTTON_HEIGHT, slider);
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        renderTitle(guiGraphics, TITLE);
        renderMultilineText(guiGraphics, DESCRIPTION);

        Component sliderTooltip = slider.getHoverText();
        Component testTooltip = micTestButton.getHoverText();
        if (slider.isHovered() && sliderTooltip != null) {
            guiGraphics.renderTooltip(font, sliderTooltip, mouseX, mouseY);
        } else if (micTestButton.isHovered() && testTooltip != null) {
            guiGraphics.renderTooltip(font, testTooltip, mouseX, mouseY);
        }
    }
}
