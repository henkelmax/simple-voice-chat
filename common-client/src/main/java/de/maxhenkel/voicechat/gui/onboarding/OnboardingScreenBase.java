package de.maxhenkel.voicechat.gui.onboarding;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;

public abstract class OnboardingScreenBase extends Screen {

    public static final Component NEXT = Component.translatable("message.voicechat.onboarding.next");
    public static final Component BACK = Component.translatable("message.voicechat.onboarding.back");
    public static final Component CANCEL = Component.translatable("message.voicechat.onboarding.cancel");

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
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Nullable
    public Screen getNextScreen() {
        return null;
    }

    protected void addPositiveButton(Component text, Button.OnPress onPress) {
        Button nextButton = Button.builder(text, onPress).bounds(guiLeft + contentWidth / 2 + PADDING / 2, guiTop + contentHeight - BUTTON_HEIGHT, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT).build();
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
        Button cancel = Button.builder(text, button -> {
            minecraft.setScreen(previous);
        }).bounds(guiLeft, guiTop + contentHeight - BUTTON_HEIGHT, big ? contentWidth : contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT).build();
        addRenderableWidget(cancel);
    }

    protected void addBackOrCancelButton() {
        addBackOrCancelButton(false);
    }

    protected void renderTitle(GuiGraphics guiGraphics, Component titleComponent) {
        int titleWidth = font.width(titleComponent);
        guiGraphics.drawString(font, titleComponent.getVisualOrderText(), width / 2 - titleWidth / 2, guiTop, TEXT_COLOR, true);
    }

    protected void renderMultilineText(GuiGraphics guiGraphics, Component textComponent) {
        List<FormattedCharSequence> text = font.split(textComponent, contentWidth);

        for (int i = 0; i < text.size(); i++) {
            FormattedCharSequence line = text.get(i);
            guiGraphics.drawString(font, line, width / 2 - font.width(line) / 2, guiTop + font.lineHeight + 20 + i * (font.lineHeight + 1), TEXT_COLOR, true);
        }
    }

}
