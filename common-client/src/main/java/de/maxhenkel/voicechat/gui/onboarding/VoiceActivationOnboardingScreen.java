package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.natives.SpeexManager;
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

        int bottom = guiTop + contentHeight - PADDING * 2 - BUTTON_HEIGHT - 15;
        int space = BUTTON_HEIGHT + SMALL_PADDING;

        boolean agc = SpeexManager.canUseAgc();
        MicAmplificationSlider micAmp = new MicAmplificationSlider(guiLeft + (agc ? 80 + 1 : 0), bottom - space * 3, contentWidth - (agc ? 80 : 0) - 1, BUTTON_HEIGHT);
        if (agc) {
            addRenderableWidget(new AgcButton(guiLeft, bottom - space * 3, 80, BUTTON_HEIGHT, active -> micAmp.setActive(!active)));
        }
        addRenderableWidget(micAmp);
        addRenderableWidget(new DenoiserButton(guiLeft, bottom - space * 2, contentWidth, BUTTON_HEIGHT));

        addRenderableWidget(new VadButton(guiLeft + 20 + SMALL_PADDING, bottom - space, contentWidth - 20 - SMALL_PADDING, BUTTON_HEIGHT));
        slider = new VoiceActivationSlider(guiLeft, bottom, contentWidth, BUTTON_HEIGHT);
        micTestButton = new MicTestButton(guiLeft, bottom - space, false, slider);
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
