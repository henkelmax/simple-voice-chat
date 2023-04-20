package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

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
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        guiGraphics.blitNineSliced(SLIDER_LOCATION, getX(), getY(), getWidth(), getHeight(), 4, 200, 20, 0, getYTexturePos());

        int micWidth = (int) (226D * micValue);
        guiGraphics.blit(VOICE_ACTIVATION_SLIDER, getX() + 1, getY() + 1, 0, 0, micWidth, 18);

        guiGraphics.blitNineSliced(SLIDER_LOCATION, getX() + (int) (value * (double) (width - 8)), getY(), 8, 20, 4, 200, 20, 0, getYtexturePosHandle());
        int color = active ? 16777215 : 10526880;
        renderScrollingString(guiGraphics, minecraft.font, 2, color);
    }

    private int getYTexturePos() {
        return (isFocused() && !(isHovered || isFocused()) ? 1 : 0) * 20;
    }

    private int getYtexturePosHandle() {
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
    @Override
    public Tooltip getTooltip() {
        if (value >= 1D) {
            return Tooltip.create(NO_ACTIVATION);
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
