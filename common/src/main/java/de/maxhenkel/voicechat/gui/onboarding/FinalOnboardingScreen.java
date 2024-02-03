package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.*;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

public class FinalOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TranslationTextComponent("message.voicechat.onboarding.final").withStyle(TextFormatting.BOLD);
    private static final ITextComponent FINISH_SETUP = new TranslationTextComponent("message.voicechat.onboarding.final.finish_setup");

    protected ITextComponent description;

    public FinalOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
        description = new StringTextComponent("");
    }

    @Override
    protected void init() {
        super.init();

        IFormattableTextComponent text = new TranslationTextComponent("message.voicechat.onboarding.final.description.success",
                KeyEvents.KEY_VOICE_CHAT.getTranslatedKeyMessage().copy().withStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)
        ).append("\n\n");

        if (VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.PTT)) {
            text = text.append(new TranslationTextComponent("message.voicechat.onboarding.final.description.ptt",
                    KeyEvents.KEY_PTT.getTranslatedKeyMessage().copy().withStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)
            ).withStyle(TextFormatting.BOLD)).append("\n\n");
        } else {
            text = text.append(new TranslationTextComponent("message.voicechat.onboarding.final.description.voice",
                    KeyEvents.KEY_MUTE.getTranslatedKeyMessage().copy().withStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE)
            ).withStyle(TextFormatting.BOLD)).append("\n\n");
        }

        description = text.append(new TranslationTextComponent("message.voicechat.onboarding.final.description.configuration"));

        addPositiveButton(FINISH_SETUP, button -> OnboardingManager.finishOnboarding());
        addBackOrCancelButton();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTitle(stack, TITLE);
        renderMultilineText(stack, description);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            OnboardingManager.finishOnboarding();
            return true;
        }
        if (keyCode == ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_VOICE_CHAT).getValue()) {
            OnboardingManager.finishOnboarding();
            minecraft.setScreen(new VoiceChatScreen());
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

}
