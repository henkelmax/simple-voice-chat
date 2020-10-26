package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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
        ySize = 201;
    }

    @Override
    protected void func_231160_c_() {
        super.func_231160_c_();
        this.guiLeft = (field_230708_k_ - this.xSize) / 2;
        this.guiTop = (field_230709_l_ - this.ySize) / 2;

        voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10, guiTop + 95, xSize - 20, 20);
        func_230480_a_(new VoiceSoundSlider(guiLeft + 10, guiTop + 20, xSize - 20, 20));
        func_230480_a_(new MicAmplificationSlider(guiLeft + 10, guiTop + 45, xSize - 20, 20));
        func_230480_a_(new MicActivationButton(guiLeft + 10, guiTop + 70, xSize - 20, 20, voiceActivationSlider));
        func_230480_a_(voiceActivationSlider);
        func_230480_a_(new MicTestButton(guiLeft + 10, guiTop + 145, xSize - 20, 20, this));
        func_230480_a_(new Button(guiLeft + 10, guiTop + 170, xSize - 20, 20, new TranslationTextComponent("message.adjust_volumes"), button -> {
            field_230706_i_.displayGuiScreen(new AdjustVolumeScreen());
        }));
    }

    @Override
    public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
        if (keyCode == field_230706_i_.gameSettings.keyBindInventory.getKey().getKeyCode() || keyCode == Main.KEY_VOICE_CHAT_SETTINGS.getKey().getKeyCode()) {
            field_230706_i_.displayGuiScreen(null);
            return true;
        }
        return super.func_231046_a_(keyCode, scanCode, modifiers);
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

        RenderSystem.color4f(1F, 1F, 1F, 1F);
        field_230706_i_.getTextureManager().bindTexture(TEXTURE);
        func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);

        func_238474_b_(matrixStack, guiLeft + 10, guiTop + 120, 0, 219, xSize - 20, 20);
        func_238474_b_(matrixStack, guiLeft + 11, guiTop + 121, 0, 201, (int) ((xSize - 18) * micValue), 18);

        int pos = (int) ((xSize - 20) * Utils.dbToPerc(Main.CLIENT_CONFIG.voiceActivationThreshold.get()));

        func_238474_b_(matrixStack, guiLeft + 10 + pos, guiTop + 120, 0, 219, 1, 20);

        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

        // Title
        ITextComponent title = new TranslationTextComponent("gui.voice_chat_settings.title");
        int titleWidth = field_230712_o_.getStringWidth(title.getString());
        field_230712_o_.func_238422_b_(matrixStack, title.func_241878_f(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }

    @Override
    public void onMicValue(double perc) {
        this.micValue = perc;
    }
}
