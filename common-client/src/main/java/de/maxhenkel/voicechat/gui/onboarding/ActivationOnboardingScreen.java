package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ButtonBase;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class ActivationOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.activation.title").setStyle(new Style().setBold(true));
    private static final ITextComponent DESCRIPTION = new TextComponentTranslation("message.voicechat.onboarding.activation")
            .appendText("\n\n")
            .appendSibling(new TextComponentTranslation("message.voicechat.onboarding.activation.ptt", new TextComponentTranslation("message.voicechat.onboarding.activation.ptt.name").setStyle(new Style().setBold(true).setUnderlined(true))))
            .appendText("\n\n")
            .appendSibling(new TextComponentTranslation("message.voicechat.onboarding.activation.voice", new TextComponentTranslation("message.voicechat.onboarding.activation.voice.name").setStyle(new Style().setBold(true).setUnderlined(true))));

    public ActivationOnboardingScreen(@Nullable GuiScreen previous) {
        super(TITLE, previous);
    }

    @Override
    public void initGui() {
        super.initGui();

        ButtonBase ptt = new ButtonBase(0, guiLeft, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, new TextComponentTranslation("message.voicechat.onboarding.activation.ptt.name")) {
            @Override
            public void onPress() {
                VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.PTT).save();
                mc.displayGuiScreen(new PttOnboardingScreen(ActivationOnboardingScreen.this));
            }
        };
        addButton(ptt);

        ButtonBase voice = new ButtonBase(1, guiLeft + contentWidth / 2 + PADDING / 2, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, new TextComponentTranslation("message.voicechat.onboarding.activation.voice.name")) {
            @Override
            public void onPress() {
                VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.VOICE).save();
                mc.displayGuiScreen(new VoiceActivationOnboardingScreen(ActivationOnboardingScreen.this));
            }
        };
        addButton(voice);

        addBackOrCancelButton(2, true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderTitle(TITLE);
        renderMultilineText(DESCRIPTION);
    }
}
