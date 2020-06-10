package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceChatScreen extends Screen {

    protected static final int FONT_COLOR = 4210752;

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_voicechat.png");

    private int guiLeft;
    private int guiTop;
    private int xSize;
    private int ySize;

    public VoiceChatScreen() {
        super(new TranslationTextComponent("gui.voice_chat_settings.title"));
        xSize = 248;
        ySize = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        addButton(new VoiceSoundSlider(guiLeft + 10, guiTop + 20, xSize - 20, 20));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(TEXTURE);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);

        super.render(mouseX, mouseY, partialTicks);

        // Title
        String title = new TranslationTextComponent("gui.voice_chat_settings.title").getFormattedText();
        int titleWidth = font.getStringWidth(title);
        font.drawString(title, (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);


    }
}
