package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.voice.client.AutomaticGainControl;
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

        boolean agc = AutomaticGainControl.canUseAgc();
        MicAmplificationSlider micAmp = new MicAmplificationSlider(guiLeft + (agc ? 80 + 1 : 0), bottom - space * 2, contentWidth - (agc ? 80 : 0) - 1, BUTTON_HEIGHT);
        if (agc) {
            addRenderableWidget(new AgcButton(guiLeft, bottom - space * 2, 80, BUTTON_HEIGHT, active -> micAmp.active = !active));
        }
        addRenderableWidget(micAmp);
        addRenderableWidget(new DenoiserButton(guiLeft, bottom - space, contentWidth, BUTTON_HEIGHT));

        slider = new VoiceActivationSlider(guiLeft + 20 + SMALL_PADDING, bottom, contentWidth - 20 - SMALL_PADDING, BUTTON_HEIGHT);
        micTestButton = new MicTestButton(guiLeft, bottom, slider);
        addRenderableWidget(micTestButton);
        addRenderableWidget(slider);

        addBackOrCancelButton();
        addNextButton();
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
        if (slider.isHovered() && sliderTooltip != null) {
            guiGraphics.setTooltipForNextFrame(font, sliderTooltip, mouseX, mouseY);
        }
    }

}
