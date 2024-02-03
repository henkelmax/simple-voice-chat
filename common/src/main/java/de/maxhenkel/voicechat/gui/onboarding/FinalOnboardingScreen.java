package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.*;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;

public class FinalOnboardingScreen extends OnboardingScreenBase {

    private static final ITextComponent TITLE = new TextComponentTranslation("message.voicechat.onboarding.final").setStyle(new Style().setBold(true));
    private static final ITextComponent FINISH_SETUP = new TextComponentTranslation("message.voicechat.onboarding.final.finish_setup");

    protected ITextComponent description;

    public FinalOnboardingScreen(@Nullable GuiScreen previous) {
        super(TITLE, previous);
        description = new TextComponentString("");
    }

    @Override
    public void initGui() {
        super.initGui();

        ITextComponent text = new TextComponentTranslation("message.voicechat.onboarding.final.description.success",
                new TextComponentString(KeyEvents.KEY_VOICE_CHAT.getDisplayName()).setStyle(new Style().setBold(true).setUnderlined(true))
        ).appendText("\n\n");

        if (VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.PTT)) {
            text = text.appendSibling(new TextComponentTranslation("message.voicechat.onboarding.final.description.ptt",
                    new TextComponentString(KeyEvents.KEY_PTT.getDisplayName()).setStyle(new Style().setBold(true).setUnderlined(true))
            ).setStyle(new Style().setBold(true))).appendText("\n\n");
        } else {
            text = text.appendSibling(new TextComponentTranslation("message.voicechat.onboarding.final.description.voice",
                    new TextComponentString(KeyEvents.KEY_MUTE.getDisplayName()).setStyle(new Style().setBold(true).setUnderlined(true))
            ).setStyle(new Style().setBold(true))).appendText("\n\n");
        }

        description = text.appendSibling(new TextComponentTranslation("message.voicechat.onboarding.final.description.configuration"));

        addPositiveButton(0, FINISH_SETUP, button -> OnboardingManager.finishOnboarding());
        addBackOrCancelButton(1);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderTitle(TITLE);
        renderMultilineText(description);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            OnboardingManager.finishOnboarding();
            return;
        }
        if (keyCode == ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_VOICE_CHAT)) {
            OnboardingManager.finishOnboarding();
            mc.displayGuiScreen(new VoiceChatScreen());
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

}
