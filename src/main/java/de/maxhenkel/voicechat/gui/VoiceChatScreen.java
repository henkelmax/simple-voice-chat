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
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
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
        @Nullable Client client = VoicechatClient.CLIENT.getClient();

        mute = new ToggleImageButton(guiLeft + 6, guiTop + ySize - 6 - 20, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.mute_microphone").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(mute);

        ToggleImageButton disable = new ToggleImageButton(guiLeft + 6 + 20 + 2, guiTop + ySize - 6 - 20, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.disable_voice_chat").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(disable);

        if (client != null && client.allowRecording()) {
            ToggleImageButton record = new ToggleImageButton(guiLeft + xSize - 6 - 20 - 2 - 20, guiTop + ySize - 6 - 20, RECORD, () -> VoicechatClient.CLIENT.getClient() != null && VoicechatClient.CLIENT.getClient().getRecorder() != null, button -> {
                Client c = VoicechatClient.CLIENT.getClient();
                if (c == null) {
                    return;
                }
                c.toggleRecording();
            }, (button, matrices, mouseX, mouseY) -> {
                renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.toggle_recording").getVisualOrderText()), mouseX, mouseY);
            });
            addButton(record);
        }

        ToggleImageButton hide = new ToggleImageButton(guiLeft + xSize - 6 - 20, guiTop + ySize - 6 - 20, HIDE, VoicechatClient.CLIENT_CONFIG.hideIcons::get, button -> {
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(!VoicechatClient.CLIENT_CONFIG.hideIcons.get()).save();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.hide_icons").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(hide);

        Button settings = new Button(guiLeft + 6, guiTop + 6 + 15, 75, 20, new TextComponent("Settings"), button -> {
            minecraft.setScreen(new VoiceChatSettingsScreen());
        });
        addButton(settings);

        Button group = new Button(guiLeft + xSize - 6 - 75 + 1, guiTop + 6 + 15, 75, 20, new TextComponent("Group"), button -> {
            if (stateManager.isInGroup()) {
                minecraft.setScreen(new GroupScreen());
            } else {
                minecraft.setScreen(new CreateGroupScreen());
            }
        });
        addButton(group);

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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == KeyBindingHelper.getBoundKeyOf(VoicechatClient.KEY_VOICE_CHAT).getValue()) {
            minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        Component title = new TranslatableComponent("gui.voicechat.voice_chat.title");
        int titleWidth = font.width(title);
        font.draw(poseStack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);

        Client client = VoicechatClient.CLIENT.getClient();
        if (client != null && client.getRecorder() != null) {
            AudioRecorder recorder = client.getRecorder();
            TextComponent time = new TextComponent(recorder.getDuration());
            font.draw(poseStack, time.withStyle(ChatFormatting.DARK_RED), (float) (guiLeft + (xSize - font.width(time)) / 2), guiTop + ySize - font.lineHeight - 7, 0);

            if (recordingHoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                renderTooltip(poseStack, new TranslatableComponent("message.voicechat.storage_size", recorder.getStorage()), mouseX, mouseY);
            }
        }
    }

}
