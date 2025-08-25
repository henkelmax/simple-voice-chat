package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.voice.client.AutomaticGainControl;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class VoiceActivationOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.voice.title").setStyle(new Style().setBold(true));
    private static final ITextComponent DESCRIPTION = new TextComponentTranslation("message.voicechat.onboarding.voice.description");

    protected VoiceActivationSlider slider;
    protected MicTestButton micTestButton;

    public VoiceActivationOnboardingScreen(@Nullable GuiScreen previous) {
        super(TITLE, previous);
    }

    @Override
    public void initGui() {
        super.initGui();

        int bottom = guiTop + contentHeight - PADDING * 2 - BUTTON_HEIGHT - 15;
        int space = BUTTON_HEIGHT + SMALL_PADDING;

        boolean agc = AutomaticGainControl.canUseAgc();
        MicAmplificationSlider micAmp = new MicAmplificationSlider(0, guiLeft + (agc ? 80 + 1 : 0), bottom - space * 3, contentWidth - (agc ? 80 : 0) - 1, BUTTON_HEIGHT);
        if (agc) {
            addButton(new AgcButton(1, guiLeft, bottom - space * 3, 80, BUTTON_HEIGHT, active -> micAmp.enabled = !active));
        }
        addButton(micAmp);
        addButton(new DenoiserButton(2, guiLeft, bottom - space * 2, contentWidth, BUTTON_HEIGHT));

        addButton(new VadButton(3, guiLeft + 20 + SMALL_PADDING, bottom - space, contentWidth - 20 - SMALL_PADDING, BUTTON_HEIGHT));
        slider = new VoiceActivationSlider(4, guiLeft, bottom, contentWidth, BUTTON_HEIGHT);
        micTestButton = new MicTestButton(5, guiLeft, bottom - space, false, slider);
        addButton(micTestButton);
        addButton(slider);

        addBackOrCancelButton(6);
        addNextButton(7);
    }

    @Override
    public GuiScreen getNextScreen() {
        return new FinalOnboardingScreen(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        renderTitle(TITLE);
        renderMultilineText(DESCRIPTION);

        ITextComponent sliderTooltip = slider.getHoverText();
        if (slider.isHovered() && sliderTooltip != null) {
            drawHoveringText(sliderTooltip.getFormattedText(), mouseX, mouseY);
        } else if (micTestButton.isHovered()) {
            micTestButton.onTooltip(micTestButton, mouseX, mouseY);
        }
    }
}
