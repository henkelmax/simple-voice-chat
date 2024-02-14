package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.widgets.KeybindButton;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class PttOnboardingScreen extends OnboardingScreenBase {

    private static final Component TITLE = new TranslatableComponent("message.voicechat.onboarding.ptt.title").withStyle(ChatFormatting.BOLD);
    private static final Component DESCRIPTION = new TranslatableComponent("message.voicechat.onboarding.ptt.description");
    private static final Component BUTTON_DESCRIPTION = new TranslatableComponent("message.voicechat.onboarding.ptt.button_description");

    protected KeybindButton keybindButton;

    protected int keybindButtonPos;

    public PttOnboardingScreen(@Nullable Screen previous) {
        super(TITLE, previous);
    }

    @Override
    protected void init() {
        super.init();

        keybindButtonPos = guiTop + contentHeight - BUTTON_HEIGHT * 3 - PADDING * 2 - 40;
        keybindButton = new KeybindButton(KeyEvents.KEY_PTT, guiLeft + 40, keybindButtonPos, contentWidth - 40 * 2, BUTTON_HEIGHT);
        addRenderableWidget(keybindButton);

        addBackOrCancelButton();
        addNextButton();
    }

    @Override
    public Screen getNextScreen() {
        return new FinalOnboardingScreen(this);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        renderTitle(poseStack, TITLE);
        renderMultilineText(poseStack, DESCRIPTION);
        font.drawShadow(poseStack, BUTTON_DESCRIPTION.getVisualOrderText(), width / 2 - font.width(BUTTON_DESCRIPTION) / 2, keybindButtonPos - font.lineHeight - PADDING, TEXT_COLOR);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (keybindButton.isListening()) {
            return false;
        }
        return super.shouldCloseOnEsc();
    }
}
