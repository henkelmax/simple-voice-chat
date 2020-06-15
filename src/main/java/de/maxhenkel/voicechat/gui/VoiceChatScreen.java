package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceChatScreen extends Screen implements MicTestButton.MicListener {

    protected static final int FONT_COLOR = 4210752;

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_voicechat.png");

    private int guiLeft;
    private int guiTop;
    private int xSize;
    private int ySize;

    private double micValue;

    private VoiceActivationSlider voiceActivationSlider;

    public VoiceChatScreen() {
        super(new TranslationTextComponent("gui.voice_chat_settings.title"));
        xSize = 248;
        ySize = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10, guiTop + 95, xSize - 20, 20);
        addButton(new VoiceSoundSlider(guiLeft + 10, guiTop + 20, xSize - 20, 20));
        addButton(new MicAmplificationSlider(guiLeft + 10, guiTop + 45, xSize - 20, 20));
        addButton(new MicActivationButton(guiLeft + 10, guiTop + 70, xSize - 20, 20, voiceActivationSlider));
        addButton(voiceActivationSlider);
        addButton(new MicTestButton(guiLeft + 10, guiTop + 145, xSize - 20, 20, this));
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
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(TEXTURE);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);

        blit(guiLeft + 10, guiTop + 120, 0, 194, xSize - 20, 20);
        blit(guiLeft + 11, guiTop + 121, 0, 176, (int) ((xSize - 18) * micValue), 18);

        super.render(mouseX, mouseY, partialTicks);

        // Title
        String title = new TranslationTextComponent("gui.voice_chat_settings.title").getFormattedText();
        int titleWidth = font.getStringWidth(title);
        font.drawString(title, (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }

    @Override
    public void onMicValue(double perc) {
        this.micValue = perc;
    }
}
