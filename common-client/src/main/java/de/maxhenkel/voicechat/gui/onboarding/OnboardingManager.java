package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ChatUtils;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class OnboardingManager {

    private static final Minecraft MC = Minecraft.getInstance();

    public static boolean isOnboarding() {
        return !VoicechatClient.CLIENT_CONFIG.onboardingFinished.get();
    }

    public static void startOnboarding(@Nullable Screen parent) {
        MC.setScreen(getOnboardingScreen(parent));
    }

    public static Screen getOnboardingScreen(@Nullable Screen parent) {
        return new IntroductionOnboardingScreen(parent);
    }

    public static void finishOnboarding() {
        VoicechatClient.CLIENT_CONFIG.muted.set(true).save();
        VoicechatClient.CLIENT_CONFIG.disabled.set(false).save();
        VoicechatClient.CLIENT_CONFIG.onboardingFinished.set(true).save();
        ClientManager.getPlayerStateManager().onFinishOnboarding();
        MC.setScreen(null);
    }

    public static void onConnecting() {
        if (!isOnboarding()) {
            return;
        }
        ChatUtils.sendModMessage(new TranslatableComponent("message.voicechat.set_up",
                KeyEvents.KEY_VOICE_CHAT.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)
        ));
    }
}
