package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER = new ResourceLocation(Voicechat.MODID, "textures/gui/voice_activation_slider.png");

    private double micValue;

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, new StringTextComponent(""), Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
    }

    @Override
    protected void renderBg(MatrixStack poseStack, Minecraft minecraft, int i, int j) {
        minecraft.getTextureManager().bind(SLIDER);
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        int width = (int) (226D * micValue);
        blit(poseStack, x + 1, y + 1, 0, 0, width, 18);
        super.renderBg(poseStack, minecraft, i, j);
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(Utils.percToDb(value));
        setMessage(new TranslationTextComponent("message.voicechat.voice_activation", db));
    }

    @Override
    public void applyDebounced() {
        VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.set(Utils.percToDb(value)).save();
    }

    @Override
    public void onMicValue(double percentage) {
        this.micValue = percentage;
    }
}
