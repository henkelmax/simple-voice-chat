package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.voice.client.AudioRecorder;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;

public class VoiceChatScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_voicechat.png");
    private static final ResourceLocation MICROPHONE = new ResourceLocation(Voicechat.MODID, "textures/gui/microphone_button.png");
    private static final ResourceLocation HIDE = new ResourceLocation(Voicechat.MODID, "textures/gui/hide_button.png");
    private static final ResourceLocation SPEAKER = new ResourceLocation(Voicechat.MODID, "textures/gui/speaker_button.png");
    private static final ResourceLocation RECORD = new ResourceLocation(Voicechat.MODID, "textures/gui/record_button.png");

    private ToggleImageButton mute;
    private HoverArea recordingHoverArea;

    public VoiceChatScreen() {
        super(new TranslatableComponent("gui.voicechat.voice_chat.title"), 195, 76);
    }

    @Override
    protected void init() {
        super.init();

        ClientPlayerStateManager stateManager = VoicechatClient.CLIENT.getPlayerStateManager();

        mute = new ToggleImageButton(guiLeft + 6, guiTop + ySize - 6 - 20, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.mute_microphone").getVisualOrderText()), mouseX, mouseY);
        });
        addRenderableWidget(mute);

        ToggleImageButton disable = new ToggleImageButton(guiLeft + 6 + 20 + 2, guiTop + ySize - 6 - 20, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.disable_voice_chat").getVisualOrderText()), mouseX, mouseY);
        });
        addRenderableWidget(disable);

        ToggleImageButton record = new ToggleImageButton(guiLeft + xSize - 6 - 20 - 2 - 20, guiTop + ySize - 6 - 20, RECORD, () -> VoicechatClient.CLIENT.getClient() != null && VoicechatClient.CLIENT.getClient().getRecorder() != null, button -> {
            Client client = VoicechatClient.CLIENT.getClient();
            if (client == null) {
                return;
            }
            client.toggleRecording();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.toggle_recording").getVisualOrderText()), mouseX, mouseY);
        });
        addRenderableWidget(record);

        ToggleImageButton hide = new ToggleImageButton(guiLeft + xSize - 6 - 20, guiTop + ySize - 6 - 20, HIDE, VoicechatClient.CLIENT_CONFIG.hideIcons::get, button -> {
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(!VoicechatClient.CLIENT_CONFIG.hideIcons.get());
            VoicechatClient.CLIENT_CONFIG.hideIcons.save();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.hide_icons").getVisualOrderText()), mouseX, mouseY);
        });
        addRenderableWidget(hide);

        Button settings = new Button(guiLeft + 6, guiTop + 6 + 15, 75, 20, new TextComponent("Settings"), button -> {
            minecraft.setScreen(new VoiceChatSettingsScreen());
        });
        addRenderableWidget(settings);

        Button group = new Button(guiLeft + xSize - 6 - 75 + 1, guiTop + 6 + 15, 75, 20, new TextComponent("Group"), button -> {
            if (stateManager.isInGroup()) {
                minecraft.setScreen(new GroupScreen());
            } else {
                minecraft.setScreen(new CreateGroupScreen());
            }
        });
        addRenderableWidget(group);
        Client client = VoicechatClient.CLIENT.getClient();
        group.active = client != null && client.groupsEnabled();

        recordingHoverArea = new HoverArea(6 + 20 + 2 + 20 + 2, ySize - 6 - 20, xSize - (6 + 20 + 2 + 20 + 2) * 2, 20);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        Component title = new TranslatableComponent("gui.voicechat.voice_chat.title");
        int titleWidth = font.width(title);
        font.draw(matrixStack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);

        Client client = VoicechatClient.CLIENT.getClient();
        if (client != null && client.getRecorder() != null) {
            AudioRecorder recorder = client.getRecorder();
            TextComponent time = new TextComponent(recorder.getDuration());
            font.draw(matrixStack, time.withStyle(ChatFormatting.DARK_RED), (float) (guiLeft + (xSize - font.width(time)) / 2), guiTop + ySize - font.lineHeight - 7, 0);

            if (recordingHoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                renderTooltip(matrixStack, new TranslatableComponent("message.voicechat.storage_size", recorder.getStorage()), mouseX, mouseY);
            }
        }
    }

}
