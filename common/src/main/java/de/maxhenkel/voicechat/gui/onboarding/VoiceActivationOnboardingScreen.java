package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.gui.widgets.DenoiserButton;
import de.maxhenkel.voicechat.gui.widgets.MicAmplificationSlider;
import de.maxhenkel.voicechat.gui.widgets.MicTestButton;
import de.maxhenkel.voicechat.gui.widgets.VoiceActivationSlider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class VoiceActivationOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.voice.title").withStyle(TextFormatting.BOLD);
    private static final ITextComponent DESCRIPTION = new TranslationTextComponent("message.voicechat.onboarding.voice.description");

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

        addButton(new MicAmplificationSlider(guiLeft, bottom - space * 2, contentWidth, BUTTON_HEIGHT));
        addButton(new DenoiserButton(guiLeft, bottom - space, contentWidth, BUTTON_HEIGHT));

        slider = new VoiceActivationSlider(guiLeft + 30 + SMALL_PADDING, bottom, contentWidth - 30 - SMALL_PADDING, BUTTON_HEIGHT);
        micTestButton = new MicTestButton(guiLeft, bottom, 30, BUTTON_HEIGHT, slider);
        addButton(micTestButton);
        addButton(slider);

        addNextButton();
        addBackOrCancelButton();
    }

    @Override
    public Screen getNextScreen() {
        return new FinalOnboardingScreen(this);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);

        renderTitle(stack, TITLE);
        renderMultilineText(stack, DESCRIPTION);

        ITextComponent sliderTooltip = slider.getHoverText();
        ITextComponent testTooltip = micTestButton.getHoverText();
        if (slider.isHovered() && sliderTooltip != null) {
            renderTooltip(stack, sliderTooltip, mouseX, mouseY);
        } else if (micTestButton.isHovered() && testTooltip != null) {
            renderTooltip(stack, testTooltip, mouseX, mouseY);
        }
    }
}
