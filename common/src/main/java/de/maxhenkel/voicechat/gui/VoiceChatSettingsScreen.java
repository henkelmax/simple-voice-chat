package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.gui.volume.PlayerVolumesScreen;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VoiceChatSettingsScreen extends VoiceChatScreenBase implements MicTestButton.MicListener {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_voicechat_settings.png");
    private static final Component TITLE = new TranslatableComponent("gui.voicechat.voice_chat_settings.title");
    private static final Component ENABLED = new TranslatableComponent("message.voicechat.enabled");
    private static final Component DISABLED = new TranslatableComponent("message.voicechat.disabled");
    private static final Component ADJUST_VOLUMES = new TranslatableComponent("message.voicechat.adjust_volumes");
    private static final Component SELECT_MICROPHONE = new TranslatableComponent("message.voicechat.select_microphone");
    private static final Component SELECT_SPEAKER = new TranslatableComponent("message.voicechat.select_speaker");
    private static final Component BACK = new TranslatableComponent("message.voicechat.back");

    private double micValue;
    private VoiceActivationSlider voiceActivationSlider;
    private int thresholdY;
    @Nullable
    private Screen parent;

    public VoiceChatSettingsScreen(@Nullable Screen parent) {
        super(TITLE, 248, 219);
        this.parent = parent;
    }

    public VoiceChatSettingsScreen() {
        this(null);
    }

    @Override
    protected void init() {
        super.init();

        int y = guiTop + 20;

        addRenderableWidget(new VoiceSoundSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        addRenderableWidget(new MicAmplificationSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        BooleanConfigButton denoiser = addRenderableWidget(new BooleanConfigButton(guiLeft + 10, y, xSize - 20, 20, VoicechatClient.CLIENT_CONFIG.denoiser, enabled -> {
            return new TranslatableComponent("message.voicechat.denoiser", enabled ? ENABLED : DISABLED);
        }));
        if (Denoiser.createDenoiser() == null) {
            denoiser.active = false;
        }
        y += 21;

        voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10, y + 21, xSize - 20, 20);

        addRenderableWidget(new MicActivationButton(guiLeft + 10, y, xSize - 20, 20, voiceActivationSlider));
        y += 42;
        thresholdY = y;

        addRenderableWidget(voiceActivationSlider);
        y += 21;

        addRenderableWidget(new MicTestButton(guiLeft + 10, y, xSize - 20, 20, this));
        y += 21;
        if (isIngame()) {
            addRenderableWidget(new Button(guiLeft + 10, y, xSize - 20, 20, ADJUST_VOLUMES, button -> {
                minecraft.setScreen(new PlayerVolumesScreen());
            }));
            y += 21;
        }
        addRenderableWidget(new Button(guiLeft + 10, y, xSize / 2 - 15, 20, SELECT_MICROPHONE, button -> {
            minecraft.setScreen(new SelectMicrophoneScreen(this));
        }));
        addRenderableWidget(new Button(guiLeft + xSize / 2 + 6, y, xSize / 2 - 15, 20, SELECT_SPEAKER, button -> {
            minecraft.setScreen(new SelectSpeakerScreen(this));
        }));
        y += 21;
        if (!isIngame() && parent != null) {
            addRenderableWidget(new Button(guiLeft + 10, y, xSize - 20, 20, BACK, button -> {
                minecraft.setScreen(parent);
            }));
        }
    }

    @Override
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        if (isIngame()) {
            blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
        }

        blit(poseStack, guiLeft + 10, thresholdY, 0, 237, xSize - 20, 20, 512, 512);
        blit(poseStack, guiLeft + 11, thresholdY + 1, 0, 219, (int) ((xSize - 18) * micValue), 18, 512, 512);

        int pos = (int) ((xSize - 20) * Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get()));

        blit(poseStack, guiLeft + 10 + pos, thresholdY, 0, 237, 1, 20, 512, 512);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        int titleWidth = font.width(TITLE);
        font.draw(poseStack, TITLE.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, getFontColor());
    }

    @Override
    public void onMicValue(double perc) {
        this.micValue = perc;
    }
}
