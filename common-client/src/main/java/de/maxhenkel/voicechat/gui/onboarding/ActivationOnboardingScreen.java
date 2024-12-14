package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class ActivationOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.activation.title").withStyle(TextFormatting.BOLD);
    private static final ITextComponent DESCRIPTION = new TranslationTextComponent("message.voicechat.onboarding.activation")
            .append("\n\n")
            .append(new TranslationTextComponent("message.voicechat.onboarding.activation.ptt", new TranslationTextComponent("message.voicechat.onboarding.activation.ptt.name").withStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)))
            .append("\n\n")
            .append(new TranslationTextComponent("message.voicechat.onboarding.activation.voice", new TranslationTextComponent("message.voicechat.onboarding.activation.voice.name").withStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)));

    public ActivationOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {
        super.init();

        Button ptt = new Button(guiLeft, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, new TranslationTextComponent("message.voicechat.onboarding.activation.ptt.name"), button -> {
            VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.PTT).save();
            minecraft.setScreen(new PttOnboardingScreen(this));
        });
        addButton(ptt);

        Button voice = new Button(guiLeft + contentWidth / 2 + PADDING / 2, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, new TranslationTextComponent("message.voicechat.onboarding.activation.voice.name"), button -> {
            VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.VOICE).save();
            minecraft.setScreen(new VoiceActivationOnboardingScreen(this));
        });
        addButton(voice);

        addBackOrCancelButton(true);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTitle(stack, TITLE);
        renderMultilineText(stack, DESCRIPTION);
    }
}
