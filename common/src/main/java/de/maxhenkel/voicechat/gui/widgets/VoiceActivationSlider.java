package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VoiceActivationSlider extends DebouncedSlider implements MicTestButton.MicListener {

    private static final ResourceLocation SLIDER_LOCATION = new ResourceLocation("textures/gui/slider.png");
    private static final ResourceLocation VOICE_ACTIVATION_SLIDER = new ResourceLocation(Voicechat.MODID, "textures/gui/voice_activation_slider.png");
    private static final Component NO_ACTIVATION = Component.translatable("message.voicechat.voice_activation.disabled").withStyle(ChatFormatting.RED);

    private double micValue;

    public VoiceActivationSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get().floatValue()));
        updateMessage();
    }

    @Override
    public void renderWidget(PoseStack poseStack, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, SLIDER_LOCATION);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        blitNineSliced(poseStack, getX(), getY(), getWidth(), getHeight(), 4, 200, 20, 0, getTextureY());

        RenderSystem.setShaderTexture(0, VOICE_ACTIVATION_SLIDER);
        int micWidth = (int) (226D * micValue);
        blit(poseStack, getX() + 1, getY() + 1, 0, 0, micWidth, 18);

        RenderSystem.setShaderTexture(0, SLIDER_LOCATION);

        blitNineSliced(poseStack, getX() + (int) (value * (double) (width - 8)), getY(), 8, 20, 4, 200, 20, 0, getHandleTextureY());
        int color = active ? 16777215 : 10526880;
        renderScrollingString(poseStack, minecraft.font, 2, color);
    }

    private int getTextureY() {
        return (isFocused() && !(isHovered || isFocused()) ? 1 : 0) * 20;
    }

    private int getHandleTextureY() {
        return (!isHovered && !isFocused() ? 2 : 3) * 20;
    }

    @Override
    protected void updateMessage() {
        long db = Math.round(Utils.percToDb(value));
        MutableComponent component = Component.translatable("message.voicechat.voice_activation", db);

        if (db >= -10L) {
            component.withStyle(ChatFormatting.RED);
        }

        setMessage(component);
    }

    @Nullable
    public Component getTooltip() {
        if (value >= 1D) {
            return NO_ACTIVATION;
        }
        return null;
    }

    public boolean isHovered() {
        return isHovered;
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
