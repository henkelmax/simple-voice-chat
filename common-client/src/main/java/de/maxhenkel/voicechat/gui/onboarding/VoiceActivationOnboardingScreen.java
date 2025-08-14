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

        int bottom = guiTop + contentHeight - PADDING * 3 - BUTTON_HEIGHT * 2;
        int space = BUTTON_HEIGHT + SMALL_PADDING;

        boolean agc = AutomaticGainControl.canUseAgc();
        MicAmplificationSlider micAmp = new MicAmplificationSlider(0, guiLeft + (agc ? 80 + 1 : 0), bottom - space * 2, contentWidth - (agc ? 80 : 0) - 1, BUTTON_HEIGHT);
        if (agc) {
            addButton(new AgcButton(guiLeft, bottom - space * 2, 80, BUTTON_HEIGHT, active -> micAmp.active = !active));
        }
        addButton(micAmp);
        addButton(new DenoiserButton(1, guiLeft, bottom - space, contentWidth, BUTTON_HEIGHT));

        slider = new VoiceActivationSlider(2, guiLeft + 20 + SMALL_PADDING, bottom, contentWidth - 20 - SMALL_PADDING, BUTTON_HEIGHT);
        micTestButton = new MicTestButton(3, guiLeft, bottom, slider);
        addButton(micTestButton);
        addButton(slider);

        addBackOrCancelButton(4);
        addNextButton(5);
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
