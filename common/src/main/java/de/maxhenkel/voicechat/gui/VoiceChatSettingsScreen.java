package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectMicrophoneScreen;
import de.maxhenkel.voicechat.gui.audiodevice.SelectSpeakerScreen;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumesScreen;
import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.Denoiser;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import de.maxhenkel.voicechat.voice.client.speaker.AudioType;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class VoiceChatSettingsScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_voicechat_settings.png");
    private static final ITextComponent TITLE = new TextComponentTranslation("gui.voicechat.voice_chat_settings.title");
    private static final ITextComponent ENABLED = new TextComponentTranslation("message.voicechat.enabled");
    private static final ITextComponent DISABLED = new TextComponentTranslation("message.voicechat.disabled");
    private static final ITextComponent ADJUST_VOLUMES = new TextComponentTranslation("message.voicechat.adjust_volumes");
    private static final ITextComponent SELECT_MICROPHONE = new TextComponentTranslation("message.voicechat.select_microphone");
    private static final ITextComponent SELECT_SPEAKER = new TextComponentTranslation("message.voicechat.select_speaker");
    private static final ITextComponent BACK = new TextComponentTranslation("message.voicechat.back");

    @Nullable
    private final GuiScreen parent;
    private VoiceActivationSlider voiceActivationSlider;

    public VoiceChatSettingsScreen(@Nullable GuiScreen parent) {
        super(TITLE, 248, 219);
        this.parent = parent;
    }

    public VoiceChatSettingsScreen() {
        this(null);
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 20;

        addButton(new VoiceSoundSlider(0, guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        addButton(new MicAmplificationSlider(1, guiLeft + 10, y, xSize - 20, 20));
        y += 21;
        BooleanConfigButton denoiser = addButton(new BooleanConfigButton(2, guiLeft + 10, y, xSize - 20, 20, VoicechatClient.CLIENT_CONFIG.denoiser, enabled -> {
            return new TextComponentTranslation("message.voicechat.denoiser", enabled ? ENABLED : DISABLED);
        }));
        if (Denoiser.createDenoiser() == null) {
            denoiser.enabled = false;
        }
        y += 21;

        voiceActivationSlider = new VoiceActivationSlider(3, guiLeft + 10, y + 21, xSize - 20, 20);

        addButton(new MicActivationButton(4, guiLeft + 10, y, xSize - 20, 20, voiceActivationSlider));
        y += 21;

        addButton(voiceActivationSlider);
        y += 21;

        MicTestButton micTestButton = new MicTestButton(5, guiLeft + 10, y, xSize - 20, 20, voiceActivationSlider);
        addButton(micTestButton);
        y += 21;

        addButton(new EnumButton<AudioType>(6, guiLeft + 10, y, xSize - 20, 20, VoicechatClient.CLIENT_CONFIG.audioType) {

            @Override
            protected ITextComponent getText(AudioType type) {
                return new TextComponentTranslation("message.voicechat.audio_type", type.getText());
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
            addButton(new ButtonBase(7, guiLeft + 10, y, xSize - 20, 20, ADJUST_VOLUMES) {
                @Override
                public void onPress() {
                    mc.displayGuiScreen(new AdjustVolumesScreen());
                }
            });
            y += 21;
        }
        addButton(new ButtonBase(8, guiLeft + 10, y, xSize / 2 - 15, 20, SELECT_MICROPHONE) {
            @Override
            public void onPress() {
                mc.displayGuiScreen(new SelectMicrophoneScreen(VoiceChatSettingsScreen.this));
            }
        });
        addButton(new ButtonBase(9, guiLeft + xSize / 2 + 6, y, xSize / 2 - 15, 20, SELECT_SPEAKER) {
            @Override
            public void onPress() {
                mc.displayGuiScreen(new SelectSpeakerScreen(VoiceChatSettingsScreen.this));
            }
        });
        y += 21;
        if (!isIngame() && parent != null) {
            addButton(new ButtonBase(10, guiLeft + 10, y, xSize - 20, 20, BACK) {
                @Override
                public void onPress() {
                    mc.displayGuiScreen(parent);
                }
            });
        }
    }

    @Override
    public void renderBackground(int mouseX, int mouseY, float delta) {
        mc.getTextureManager().bindTexture(TEXTURE);
        if (isIngame()) {
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        }
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, float delta) {
        int titleWidth = fontRenderer.getStringWidth(TITLE.getUnformattedComponentText());
        fontRenderer.drawString(TITLE.getUnformattedComponentText(), guiLeft + (xSize - titleWidth) / 2, guiTop + 7, getFontColor());

        ITextComponent tooltip = voiceActivationSlider.getTooltip();
        if (tooltip != null && voiceActivationSlider.isMouseOver()) {
            drawHoveringText(tooltip.getUnformattedComponentText(), mouseX, mouseY);
        }
    }
}
