package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public class FinalOnboardingScreen extends OnboardingScreenBase {

    private static final Component TITLE = Component.translatable("message.voicechat.onboarding.final").withStyle(ChatFormatting.BOLD);
    private static final Component FINISH_SETUP = Component.translatable("message.voicechat.onboarding.final.finish_setup");

    protected Component description;

    public FinalOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
        description = Component.empty();
    }

    @Override
    protected void init() {
        super.init();

        MutableComponent text = Component.translatable("message.voicechat.onboarding.final.description.success",
                KeyEvents.KEY_VOICE_CHAT.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)
        ).append("\n\n");

        if (VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.PTT)) {
            text = text.append(Component.translatable("message.voicechat.onboarding.final.description.ptt",
                    KeyEvents.KEY_PTT.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)
            ).withStyle(ChatFormatting.BOLD)).append("\n\n");
        } else {
            text = text.append(Component.translatable("message.voicechat.onboarding.final.description.voice",
                    KeyEvents.KEY_MUTE.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)
            ).withStyle(ChatFormatting.BOLD)).append("\n\n");
        }

        description = text.append(Component.translatable("message.voicechat.onboarding.final.description.configuration"));

        addBackOrCancelButton();
        addPositiveButton(FINISH_SETUP, button -> OnboardingManager.finishOnboarding());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        renderTitle(poseStack, TITLE);
        renderMultilineText(poseStack, description);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE) {
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
