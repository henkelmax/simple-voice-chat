package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectMicrophoneScreen;
import de.maxhenkel.voicechat.gui.audiodevice.SelectSpeakerScreen;
import de.maxhenkel.voicechat.gui.volume.PlayerVolumesScreen;
import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import de.maxhenkel.voicechat.voice.client.speaker.AudioType;

import javax.annotation.Nullable;

public class VoiceChatSettingsScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_voicechat_settings.png");
    private static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.voice_chat_settings.title");
    private static final ITextComponent ENABLED = new TranslationTextComponent("message.voicechat.enabled");
    private static final ITextComponent DISABLED = new TranslationTextComponent("message.voicechat.disabled");
    private static final ITextComponent ADJUST_VOLUMES = new TranslationTextComponent("message.voicechat.adjust_volumes");
    private static final ITextComponent SELECT_MICROPHONE = new TranslationTextComponent("message.voicechat.select_microphone");
    private static final ITextComponent SELECT_SPEAKER = new TranslationTextComponent("message.voicechat.select_speaker");
    private static final ITextComponent BACK = new TranslationTextComponent("message.voicechat.back");

    @Nullable
    private final Screen parent;

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

        addButton(new VoiceSoundSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        addButton(new MicAmplificationSlider(guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        BooleanConfigButton denoiser = addButton(new BooleanConfigButton(guiLeft + 10, y, xSize - 20, 20, VoicechatClient.CLIENT_CONFIG.denoiser, enabled -> {
            return new TranslationTextComponent("message.voicechat.denoiser", enabled ? ENABLED : DISABLED);
        }));
        if (Denoiser.createDenoiser() == null) {
            denoiser.active = false;
        }
        y += 21;

        VoiceActivationSlider voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10, y + 21, xSize - 20, 20);

        addButton(new MicActivationButton(guiLeft + 10, y, xSize - 20, 20, voiceActivationSlider));
        y += 21;

        addButton(voiceActivationSlider);
        y += 21;

        MicTestButton micTestButton = new MicTestButton(guiLeft + 10, y, xSize - 20, 20, voiceActivationSlider);
        addButton(micTestButton);
        y += 21;

        addButton(new EnumButton<AudioType>(guiLeft + 10, y, xSize - 20, 20, VoicechatClient.CLIENT_CONFIG.audioType) {

            @Override
            protected ITextComponent getText(AudioType type) {
                return new TranslationTextComponent("message.voicechat.audio_type", type.getText());
            }

            @Override
            protected void onUpdate(AudioType type) {
                ClientVoicechat client = ClientManager.getClient();
                if (client != null) {
                    micTestButton.stop();
                    client.reloadAudio();
                }
            }
        });
        y += 21;
        if (isIngame()) {
            addButton(new Button(guiLeft + 10, y, xSize - 20, 20, ADJUST_VOLUMES, button -> {
                minecraft.setScreen(new PlayerVolumesScreen());
            }));
            y += 21;
        }
        addButton(new Button(guiLeft + 10, y, xSize / 2 - 15, 20, SELECT_MICROPHONE, button -> {
            minecraft.setScreen(new SelectMicrophoneScreen(this));
        }));
        addButton(new Button(guiLeft + xSize / 2 + 6, y, xSize / 2 - 15, 20, SELECT_SPEAKER, button -> {
            minecraft.setScreen(new SelectSpeakerScreen(this));
        }));
        y += 21;
        if (!isIngame() && parent != null) {
            addButton(new Button(guiLeft + 10, y, xSize - 20, 20, BACK, button -> {
                minecraft.setScreen(parent);
            }));
        }
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bind(TEXTURE);
        if (isIngame()) {
            blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize);
        }
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        int titleWidth = font.width(TITLE);
        font.draw(poseStack, TITLE.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, getFontColor());
    }
}
