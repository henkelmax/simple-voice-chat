package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class ActivationOnboardingScreen extends OnboardingScreenBase {

    private static final Component TITLE = new TranslatableComponent("message.voicechat.onboarding.activation.title").withStyle(ChatFormatting.BOLD);
    private static final Component DESCRIPTION = new TranslatableComponent("message.voicechat.onboarding.activation")
            .append("\n\n")
            .append(new TranslatableComponent("message.voicechat.onboarding.activation.ptt", new TranslatableComponent("message.voicechat.onboarding.activation.ptt.name").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)))
            .append("\n\n")
            .append(new TranslatableComponent("message.voicechat.onboarding.activation.voice", new TranslatableComponent("message.voicechat.onboarding.activation.voice.name").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE)));

    public ActivationOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {
        super.init();

        Button ptt = new Button(guiLeft, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, new TranslatableComponent("message.voicechat.onboarding.activation.ptt.name"), button -> {
            VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.PTT).save();
            minecraft.setScreen(new PttOnboardingScreen(this));
        });
        addRenderableWidget(ptt);

        Button voice = new Button(guiLeft + contentWidth / 2 + PADDING / 2, guiTop + contentHeight - BUTTON_HEIGHT * 2 - PADDING, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, new TranslatableComponent("message.voicechat.onboarding.activation.voice.name"), button -> {
            VoicechatClient.CLIENT_CONFIG.microphoneActivationType.set(MicrophoneActivationType.VOICE).save();
            minecraft.setScreen(new VoiceActivationOnboardingScreen(this));
        });
        addRenderableWidget(voice);

        addBackOrCancelButton(true);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        renderTitle(poseStack, TITLE);
        renderMultilineText(poseStack, DESCRIPTION);
    }
}
