package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class VoiceChatSettingsScreen extends VoiceChatScreenBase implements MicTestButton.MicListener {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_voicechat_settings.png");

    private double micValue;
    private VoiceActivationSlider voiceActivationSlider;
    private int thresholdY;
    @Nullable
    private Screen parent;

    public VoiceChatSettingsScreen(@Nullable Screen parent) {
        super(new TranslationTextComponent("gui.voicechat.voice_chat_settings.title"), 248, 219);
        this.parent = parent;
    }

    public VoiceChatSettingsScreen() {
        this(null);
    }

    @Override
    protected void init() {
        super.init();

        int y = guiTop + 20;

        addButton(new VoiceSoundSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        addButton(new MicAmplificationSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        BooleanConfigButton denoiser = addButton(new BooleanConfigButton(guiLeft + 10, y, xSize - 20, 20, VoicechatClient.CLIENT_CONFIG.denoiser, enabled -> {
            return new TranslationTextComponent("message.voicechat.denoiser",
                    enabled ? new TranslationTextComponent("message.voicechat.enabled") : new TranslationTextComponent("message.voicechat.disabled")
            );
        }));
        if (Denoiser.createDenoiser() == null) {
            denoiser.active = false;
        }
        y += 21;

        voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10, y + 21, xSize - 20, 20);

        addButton(new MicActivationButton(guiLeft + 10, y, xSize - 20, 20, voiceActivationSlider));
        y += 42;
        thresholdY = y;

        addButton(voiceActivationSlider);
        y += 21;

        addButton(new MicTestButton(guiLeft + 10, y, xSize - 20, 20, this));
        y += 21;
        if (isIngame()) {
            addButton(new Button(guiLeft + 10, y, xSize - 20, 20, new TranslationTextComponent("message.voicechat.adjust_volumes"), button -> {
                minecraft.setScreen(new AdjustVolumeScreen(this, ClientManager.getPlayerStateManager().getPlayerStates(false)));
            }));
            y += 21;
        }
        addButton(new Button(guiLeft + 10, y, xSize / 2 - 15, 20, new TranslationTextComponent("message.voicechat.select_microphone"), button -> {
            minecraft.setScreen(new SelectMicrophoneScreen(this));
        }));
        addButton(new Button(guiLeft + xSize / 2 + 6, y, xSize / 2 - 15, 20, new TranslationTextComponent("message.voicechat.select_speaker"), button -> {
            minecraft.setScreen(new SelectSpeakerScreen(this));
        }));
        y += 21;
        if (!isIngame() && parent != null) {
            addButton(new Button(guiLeft + 10, y, xSize - 20, 20, new TranslationTextComponent("message.voicechat.back"), button -> {
                minecraft.setScreen(parent);
            }));
        }
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bind(TEXTURE);
        if (isIngame()) {
            blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
        }

        blit(poseStack, guiLeft + 10, thresholdY, 0, 237, xSize - 20, 20, 512, 512);
        blit(poseStack, guiLeft + 11, thresholdY + 1, 0, 219, (int) ((xSize - 18) * micValue), 18, 512, 512);

        int pos = (int) ((xSize - 20) * Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get()));

        blit(poseStack, guiLeft + 10 + pos, thresholdY, 0, 237, 1, 20, 512, 512);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        ITextComponent title = new TranslationTextComponent("gui.voicechat.voice_chat_settings.title");
        int titleWidth = font.width(title);
        font.draw(poseStack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, getFontColor());
    }

    @Override
    public void onMicValue(double perc) {
        this.micValue = perc;
    }
}
