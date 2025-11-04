package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.audiodevice.SelectMicrophoneScreen;
import de.maxhenkel.voicechat.gui.audiodevice.SelectSpeakerScreen;
import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.natives.SpeexManager;
import de.maxhenkel.voicechat.voice.client.*;
import de.maxhenkel.voicechat.voice.client.speaker.AudioType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class VoiceChatSettingsScreen extends VoiceChatScreenBase {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/gui/gui_voicechat_settings.png");
    private static final Component TITLE = Component.translatable("gui.voicechat.voice_chat_settings.title");

    private static final Component ASSIGN_TOOLTIP = Component.translatable("message.voicechat.press_to_reassign_key");
    private static final Component PUSH_TO_TALK = Component.translatable("message.voicechat.activation_type.ptt");
    private static final Component SELECT_MICROPHONE = Component.translatable("message.voicechat.select_microphone");
    private static final Component SELECT_SPEAKER = Component.translatable("message.voicechat.select_speaker");
    private static final Component BACK = Component.translatable("message.voicechat.back");

    @Nullable
    private final Screen parent;
    private VoiceActivationSlider voiceActivationSlider;
    private MicTestButton micTestButton;
    private KeybindButton keybindButton;

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
        boolean agc = SpeexManager.canUseAgc();
        MicAmplificationSlider micAmp = new MicAmplificationSlider(guiLeft + 10 + (agc ? 80 + 1 : 0), y, xSize - 20 - (agc ? 80 : 0) - 1, 20);
        if (agc) {
            addRenderableWidget(new AgcButton(guiLeft + 10, y, 80, 20, active -> micAmp.setActive(!active)));
        }
        addRenderableWidget(micAmp);
        y += 21;
        addRenderableWidget(new DenoiserButton(guiLeft + 10, y, xSize - 20, 20));
        y += 21;

        voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10, y + 21 * 2, xSize - 20, 20);
        VadButton vadButton = new VadButton(guiLeft + 10, y + 21, xSize - 20, 20);
        micTestButton = new MicTestButton(guiLeft + 10, y, false, voiceActivationSlider);
        keybindButton = new KeybindButton(KeyEvents.KEY_PTT, guiLeft + 10, y + 21, xSize - 20, 20, PUSH_TO_TALK);
        addRenderableWidget(new MicActivationButton(guiLeft + 10 + 20 + 1, y, xSize - 20 - 20 - 1, 20, type -> {
            vadButton.visible = MicrophoneActivationType.VOICE.equals(type);
            keybindButton.visible = MicrophoneActivationType.PTT.equals(type);
            keybindButton.resetListening();
        }));

        addRenderableWidget(micTestButton);
        addRenderableWidget(vadButton);
        addRenderableWidget(voiceActivationSlider);
        addRenderableWidget(keybindButton);
        y += 21 * 3;

        addRenderableWidget(new EnumButton<>(guiLeft + 10, y, xSize - 20, 20, VoicechatClient.CLIENT_CONFIG.audioType) {
            @Override
            protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
                renderDefaultSprite(guiGraphics);
                renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
            }

            @Override
            protected Component getText(AudioType type) {
                return Component.translatable("message.voicechat.audio_type", type.getText());
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
        addRenderableWidget(Button.builder(SELECT_MICROPHONE, button -> {
            minecraft.setScreen(new SelectMicrophoneScreen(this));
        }).bounds(guiLeft + 10, y, (xSize - 20) / 2 - 1, 20).build());
        addRenderableWidget(Button.builder(SELECT_SPEAKER, button -> {
            minecraft.setScreen(new SelectSpeakerScreen(this));
        }).bounds(guiLeft + xSize / 2 + 1, y, (xSize - 20) / 2 - 1, 20).build());
        y += 21;
        if (!isIngame() && parent != null) {
            addRenderableWidget(Button.builder(BACK, button -> {
                minecraft.setScreen(parent);
            }).bounds(guiLeft + 10, y, xSize - 20, 20).build());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (keybindButton.mouseClicked(mouseButtonEvent, bl)) {
            return true;
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keybindButton.keyPressed(keyEvent)) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        if (keybindButton.keyReleased(keyEvent)) {
            return true;
        }
        return super.keyReleased(keyEvent);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (isIngame()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int titleWidth = font.width(TITLE);
        guiGraphics.drawString(font, TITLE.getVisualOrderText(), guiLeft + (xSize - titleWidth) / 2, guiTop + 7, getFontColor(), false);

        Component sliderTooltip = voiceActivationSlider.getHoverText();
        if (voiceActivationSlider.isHovered() && sliderTooltip != null) {
            guiGraphics.setTooltipForNextFrame(font, sliderTooltip, mouseX, mouseY);
        } else if (keybindButton.isHovered()) {
            guiGraphics.setTooltipForNextFrame(font, ASSIGN_TOOLTIP, mouseX, mouseY);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (keybindButton.isListening()) {
            return false;
        }
        return super.shouldCloseOnEsc();
    }
}
