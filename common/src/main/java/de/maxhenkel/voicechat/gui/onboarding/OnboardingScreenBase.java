package de.maxhenkel.voicechat.gui.onboarding;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;

public abstract class OnboardingScreenBase extends Screen {

    public static final Component NEXT = new TranslatableComponent("message.voicechat.onboarding.next");
    public static final Component BACK = new TranslatableComponent("message.voicechat.onboarding.back");
    public static final Component CANCEL = new TranslatableComponent("message.voicechat.onboarding.cancel");

    protected static final int TEXT_COLOR = 0xFFFFFFFF;
    protected static final int PADDING = 8;
    protected static final int SMALL_PADDING = 2;
    protected static final int BUTTON_HEIGHT = 20;

    protected int contentWidth;
    protected int guiLeft;
    protected int guiTop;
    protected int contentHeight;

    @Nullable
    protected Screen previous;

    public OnboardingScreenBase(Component title, @Nullable Screen previous) {
        super(title);
        this.previous = previous;
    }

    @Override
    protected void init() {
        super.init();

        contentWidth = width / 2;
        guiLeft = (width - contentWidth) / 2;
        guiTop = 20;
        contentHeight = height - guiTop * 2;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Nullable
    public Screen getNextScreen() {
        return null;
    }

    protected void addPositiveButton(Component text, Button.OnPress onPress) {
        Button nextButton = new Button(guiLeft + contentWidth / 2 + PADDING / 2, guiTop + contentHeight - BUTTON_HEIGHT, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, text, onPress);
        addRenderableWidget(nextButton);
    }

    protected void addNextButton() {
        addPositiveButton(NEXT, button -> {
            minecraft.setScreen(getNextScreen());
        });
    }

    protected void addBackOrCancelButton(boolean big) {
        Component text = CANCEL;
        if (previous instanceof OnboardingScreenBase) {
            text = BACK;
        }
        Button cancel = new Button(guiLeft, guiTop + contentHeight - BUTTON_HEIGHT, big ? contentWidth : contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, text, button -> {
            minecraft.setScreen(previous);
        });
        addRenderableWidget(cancel);
    }

    protected void addBackOrCancelButton() {
        addBackOrCancelButton(false);
    }

    protected void renderTitle(PoseStack poseStack, Component titleComponent) {
        int titleWidth = font.width(titleComponent);
        font.drawShadow(poseStack, titleComponent.getVisualOrderText(), width / 2 - titleWidth / 2, guiTop, TEXT_COLOR);
    }

    protected void renderMultilineText(PoseStack poseStack, Component textComponent) {
        List<FormattedCharSequence> text = font.split(textComponent, contentWidth);

        for (int i = 0; i < text.size(); i++) {
            FormattedCharSequence line = text.get(i);
            font.drawShadow(poseStack, line, width / 2 - font.width(line) / 2, guiTop + font.lineHeight + 20 + i * (font.lineHeight + 1), TEXT_COLOR);
        }
    }

}
