package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.stream.Collectors;

public class VoiceChatSettingsScreen extends VoiceChatScreenBase implements MicTestButton.MicListener {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_voicechat_settings.png");

    private double micValue;
    private VoiceActivationSlider voiceActivationSlider;
    private int thresholdY;

    public VoiceChatSettingsScreen() {
        super(new TranslationTextComponent("gui.voicechat.voice_chat_settings.title"), 248, 219);
    }

    @Override
    protected void init() {
        super.init();

        Client c = Main.CLIENT_VOICE_EVENTS.getClient();

        if (c == null) {
            return;
        }

        int y = guiTop + 20;

        addButton(new VoiceSoundSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        addButton(new MicAmplificationSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        if (c.getMicThread() != null && c.getMicThread().getDenoiser() != null) {
            addButton(new BooleanConfigButton(guiLeft + 10, y, xSize - 20, 20, Main.CLIENT_CONFIG.denoiser, enabled -> {
                return new TranslationTextComponent("message.voicechat.denoiser",
                        enabled ? new TranslationTextComponent("message.voicechat.enabled") : new TranslationTextComponent("message.voicechat.disabled")
                );
            }));
        }
        y += 21;

        voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10, y + 21, xSize - 20, 20);

        addButton(new MicActivationButton(guiLeft + 10, y, xSize - 20, 20, voiceActivationSlider));
        y += 42;
        thresholdY = y;

        addButton(voiceActivationSlider);
        y += 21;

        addButton(new MicTestButton(guiLeft + 10, y, xSize - 20, 20, this, c));
        y += 21;
        addButton(new Button(guiLeft + 10, y, xSize - 20, 20, new TranslationTextComponent("message.voicechat.adjust_volumes"), button -> {
            minecraft.setScreen(new AdjustVolumeScreen(this, Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().getPlayerStates().stream().filter(state -> !state.getGameProfile().getId().equals(minecraft.player.getUUID())).collect(Collectors.toList())));
        }));
        y += 21;
        addButton(new Button(guiLeft + 10, y, xSize / 2 - 15, 20, new TranslationTextComponent("message.voicechat.select_microphone"), button -> {
            minecraft.setScreen(new SelectMicrophoneScreen(this));
        }));
        addButton(new Button(guiLeft + xSize / 2 + 6, y, xSize / 2 - 15, 20, new TranslationTextComponent("message.voicechat.select_speaker"), button -> {
            minecraft.setScreen(new SelectSpeakerScreen(this));
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);

        blit(matrixStack, guiLeft + 10, thresholdY, 0, 237, xSize - 20, 20, 512, 512);
        blit(matrixStack, guiLeft + 11, thresholdY + 1, 0, 219, (int) ((xSize - 18) * micValue), 18, 512, 512);

        int pos = (int) ((xSize - 20) * Utils.dbToPerc(Main.CLIENT_CONFIG.voiceActivationThreshold.get()));

        blit(matrixStack, guiLeft + 10 + pos, thresholdY, 0, 237, 1, 20, 512, 512);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        ITextComponent title = new TranslationTextComponent("gui.voicechat.voice_chat_settings.title");
        int titleWidth = font.width(title);
        font.draw(matrixStack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }

    @Override
    public void onMicValue(double perc) {
        this.micValue = perc;
    }
}
