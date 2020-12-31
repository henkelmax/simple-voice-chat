package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ListScreen<T> extends Screen {

    protected static final int FONT_COLOR = 4210752;

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_generic_small.png");

    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    protected List<T> elements;
    protected int index;

    protected Button previous;
    protected Button back;
    protected Button next;

    public ListScreen(List<T> elements, ITextComponent title) {
        super(title);
        this.elements = elements;
        xSize = 248;
        ySize = 85;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;

        previous = new Button(guiLeft + 10, guiTop + 60, 60, 20, new TranslationTextComponent("message.previous"), button -> {
            index = (index - 1 + elements.size()) % elements.size();
            updateCurrentElement();
        });

        back = new Button(guiLeft + xSize / 2 - 30, guiTop + 60, 60, 20, new TranslationTextComponent("message.back"), button -> {
            minecraft.displayGuiScreen(new VoiceChatScreen());
        });

        next = new Button(guiLeft + xSize - 70, guiTop + 60, 60, 20, new TranslationTextComponent("message.next"), button -> {
            index = (index + 1) % elements.size();
            updateCurrentElement();
        });

        updateCurrentElement();
    }

    public void updateCurrentElement() {
        buttons.clear();
        children.clear();
        addButton(previous);
        addButton(back);
        addButton(next);

        if (elements.size() <= 1) {
            next.active = false;
            previous.active = false;
        }
    }

    @Nullable
    public T getCurrentElement() {
        if (elements.size() <= 0) {
            return null;
        }
        return elements.get(index);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == minecraft.gameSettings.keyBindInventory.getKey().getKeyCode() || keyCode == Main.KEY_VOICE_CHAT_SETTINGS.getKey().getKeyCode()) {
            minecraft.displayGuiScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bindTexture(TEXTURE);
        blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize);

        super.render(stack, mouseX, mouseY, partialTicks);

        renderText(stack, getCurrentElement(), mouseX, mouseY, partialTicks);
    }

    protected abstract void renderText(MatrixStack stack, @Nullable T element, int mouseX, int mouseY, float partialTicks);
}
