package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ChatUtils;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class OnboardingManager {

    private static final Minecraft MC = Minecraft.getMinecraft();

    public static boolean isOnboarding() {
        return !VoicechatClient.CLIENT_CONFIG.onboardingFinished.get();
    }

    public static void startOnboarding(@Nullable GuiScreen parent) {
        MC.displayGuiScreen(getOnboardingScreen(parent));
    }

    public static GuiScreen getOnboardingScreen(@Nullable GuiScreen parent) {
        return new IntroductionOnboardingScreen(parent);
    }

    public static void finishOnboarding() {
        VoicechatClient.CLIENT_CONFIG.muted.set(true).save();
        VoicechatClient.CLIENT_CONFIG.disabled.set(false).save();
        VoicechatClient.CLIENT_CONFIG.onboardingFinished.set(true).save();
        ClientManager.getPlayerStateManager().onFinishOnboarding();
        MC.displayGuiScreen(null);
    }

    public static void onConnecting() {
        if (!isOnboarding()) {
            return;
        }
        ChatUtils.sendModMessage(new TextComponentTranslation("message.voicechat.set_up",
                new TextComponentString(KeyEvents.KEY_VOICE_CHAT.getDisplayName()).setStyle(new Style().setBold(true).setUnderlined(true))
        ));
    }
}
