package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.voice.client.AudioRecorder;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;

public class VoiceChatScreen extends VoiceChatScreenBase {

    protected static final int FONT_COLOR = 4210752;

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_voicechat.png");
    private static final ResourceLocation MICROPHONE = new ResourceLocation(Main.MODID, "textures/gui/microphone_button.png");
    private static final ResourceLocation HIDE = new ResourceLocation(Main.MODID, "textures/gui/hide_button.png");
    private static final ResourceLocation SPEAKER = new ResourceLocation(Main.MODID, "textures/gui/speaker_button.png");
    private static final ResourceLocation RECORD = new ResourceLocation(Main.MODID, "textures/gui/record_button.png");

    private ToggleImageButton mute;
    private HoverArea recordingHoverArea;

    public VoiceChatScreen() {
        super(new TranslationTextComponent("gui.voicechat.voice_chat.title"), 195, 76);
    }

    @Override
    protected void init() {
        super.init();

        ClientPlayerStateManager stateManager = Main.CLIENT_VOICE_EVENTS.getPlayerStateManager();

        mute = new ToggleImageButton(guiLeft + 6, guiTop + ySize - 6 - 20, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslationTextComponent("message.voicechat.mute_microphone").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(mute);

        ToggleImageButton disable = new ToggleImageButton(guiLeft + 6 + 20 + 2, guiTop + ySize - 6 - 20, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslationTextComponent("message.voicechat.disable_voice_chat").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(disable);

        if (Main.SERVER_CONFIG.allowRecording.get()) {
            ToggleImageButton record = new ToggleImageButton(guiLeft + xSize - 6 - 20 - 2 - 20, guiTop + ySize - 6 - 20, RECORD, () -> Main.CLIENT_VOICE_EVENTS.getClient() != null && Main.CLIENT_VOICE_EVENTS.getClient().getRecorder() != null, button -> {
                Client client = Main.CLIENT_VOICE_EVENTS.getClient();
                if (client == null) {
                    return;
                }
                client.toggleRecording();
            }, (button, matrices, mouseX, mouseY) -> {
                renderTooltip(matrices, Collections.singletonList(new TranslationTextComponent("message.voicechat.toggle_recording").getVisualOrderText()), mouseX, mouseY);
            });
            addButton(record);
        }

        ToggleImageButton hide = new ToggleImageButton(guiLeft + xSize - 6 - 20, guiTop + ySize - 6 - 20, HIDE, Main.CLIENT_CONFIG.hideIcons::get, button -> {
            Main.CLIENT_CONFIG.hideIcons.set(!Main.CLIENT_CONFIG.hideIcons.get());
            Main.CLIENT_CONFIG.hideIcons.save();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslationTextComponent("message.voicechat.hide_icons").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(hide);

        Button settings = new Button(guiLeft + 6, guiTop + 6 + 15, 75, 20, new StringTextComponent("Settings"), button -> {
            minecraft.setScreen(new VoiceChatSettingsScreen());
        });
        addButton(settings);

        Button group = new Button(guiLeft + xSize - 6 - 75 + 1, guiTop + 6 + 15, 75, 20, new StringTextComponent("Group"), button -> {
            if (stateManager.isInGroup()) {
                minecraft.setScreen(new GroupScreen());
            } else {
                minecraft.setScreen(new CreateGroupScreen());
            }
        });
        addButton(group);
        group.active = Main.SERVER_CONFIG.groupsEnabled.get();

        recordingHoverArea = new HoverArea(6 + 20 + 2 + 20 + 2, ySize - 6 - 20, xSize - (6 + 20 + 2 + 20 + 2) * 2, 20);

        checkButtons();
    }

    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = Main.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        ITextComponent title = new TranslationTextComponent("gui.voicechat.voice_chat.title");
        int titleWidth = font.width(title.getString());
        font.draw(matrixStack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);

        Client client = Main.CLIENT_VOICE_EVENTS.getClient();
        if (client != null && client.getRecorder() != null) {
            AudioRecorder recorder = client.getRecorder();
            StringTextComponent time = new StringTextComponent(recorder.getDuration());
            font.draw(matrixStack, time.withStyle(TextFormatting.DARK_RED), (float) (guiLeft + (xSize - font.width(time)) / 2), guiTop + ySize - font.lineHeight - 7, 0);

            if (recordingHoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                renderTooltip(matrixStack, new TranslationTextComponent("message.voicechat.storage_size", recorder.getStorage()), mouseX, mouseY);
            }
        }
    }

}
