package de.maxhenkel.voicechat.gui.onboarding;

import de.maxhenkel.voicechat.gui.widgets.ButtonBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class OnboardingScreenBase extends ListScreenBase {

    /**
     * The screen width needs to be limited to 396 to avoid a bug in 1.12 that causes buttons to render weirdly if longer than that.
     */
    private static final int MAX_CONTENT_WIDTH = 396;

    public static final ITextComponent NEXT = new TextComponentTranslation("message.voicechat.onboarding.next");
    public static final ITextComponent BACK = new TextComponentTranslation("message.voicechat.onboarding.back");
    public static final ITextComponent CANCEL = new TextComponentTranslation("message.voicechat.onboarding.cancel");

    protected static final int TEXT_COLOR = 0xFFFFFFFF;
    protected static final int PADDING = 8;
    protected static final int SMALL_PADDING = 2;
    protected static final int BUTTON_HEIGHT = 20;

    protected int contentWidth;
    protected int guiLeft;
    protected int guiTop;
    protected int contentHeight;

    @Nullable
    protected GuiScreen previous;

    public OnboardingScreenBase(ITextComponent title, @Nullable GuiScreen previous) {
        super(title);
        this.previous = previous;
    }

    @Override
    public void initGui() {
        super.initGui();

        contentWidth = Math.min(width / 2, MAX_CONTENT_WIDTH);
        guiLeft = (width - contentWidth) / 2;
        guiTop = 20;
        contentHeight = height - guiTop * 2;
    }

    @Nullable
    public GuiScreen getNextScreen() {
        return null;
    }

    protected void addPositiveButton(int id, ITextComponent text, Consumer<ButtonBase> onPress) {
        ButtonBase nextButton = new ButtonBase(id, guiLeft + contentWidth / 2 + PADDING / 2, guiTop + contentHeight - BUTTON_HEIGHT, contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, text) {
            @Override
            public void onPress() {
                onPress.accept(this);
            }
        };
        addButton(nextButton);
    }

    protected void addNextButton(int id) {
        addPositiveButton(id, NEXT, button -> {
            mc.displayGuiScreen(getNextScreen());
        });
    }

    protected void addBackOrCancelButton(int id, boolean big) {
        ITextComponent text = CANCEL;
        if (previous instanceof OnboardingScreenBase) {
            text = BACK;
        }
        ButtonBase cancel = new ButtonBase(id, guiLeft, guiTop + contentHeight - BUTTON_HEIGHT, big ? contentWidth : contentWidth / 2 - PADDING / 2, BUTTON_HEIGHT, text) {
            @Override
            public void onPress() {
                mc.displayGuiScreen(previous);
            }
        };
        addButton(cancel);
    }

    protected void addBackOrCancelButton(int id) {
        addBackOrCancelButton(id, false);
    }

    protected void renderTitle(ITextComponent titleComponent) {
        int titleWidth = fontRenderer.getStringWidth(titleComponent.getUnformattedComponentText());
        fontRenderer.drawStringWithShadow(titleComponent.getFormattedText(), width / 2 - titleWidth / 2, guiTop, TEXT_COLOR);
    }

    protected void renderMultilineText(ITextComponent textComponent) {
        List<String> text = fontRenderer.listFormattedStringToWidth(textComponent.getFormattedText(), contentWidth).stream().flatMap(string -> Arrays.stream(string.split("\\\\n"))).collect(Collectors.toList());

        for (int i = 0; i < text.size(); i++) {
            String line = text.get(i);
            fontRenderer.drawStringWithShadow(line, width / 2 - fontRenderer.getStringWidth(line) / 2, guiTop + fontRenderer.FONT_HEIGHT + 20 + i * (fontRenderer.FONT_HEIGHT + 1), TEXT_COLOR);
        }
    }

}
