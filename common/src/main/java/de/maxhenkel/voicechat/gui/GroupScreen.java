package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.GroupList;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.LeaveGroupPacket;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.GroupChatManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;

public class GroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_group.png");
    private static final ResourceLocation LEAVE = new ResourceLocation(Voicechat.MODID, "textures/gui/leave.png");
    private static final ResourceLocation MICROPHONE = new ResourceLocation(Voicechat.MODID, "textures/gui/microphone_button.png");
    private static final ResourceLocation SPEAKER = new ResourceLocation(Voicechat.MODID, "textures/gui/speaker_button.png");
    private static final ResourceLocation GROUP_HUD = new ResourceLocation(Voicechat.MODID, "textures/gui/group_hud_button.png");

    private final ClientGroup group;
    private GroupList playerList;
    private ToggleImageButton mute;
    private ToggleImageButton disable;
    private ToggleImageButton showHUD;
    private ImageButton leave;

    public GroupScreen(ClientGroup group) {
        super(new TranslatableComponent("gui.voicechat.group.title"), 195, 222);
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        clearWidgets();

        ClientPlayerStateManager stateManager = ClientManager.getPlayerStateManager();

        playerList = new GroupList(this, 9, 16, 160, 176, GroupChatManager::getGroupMembers);

        mute = new ToggleImageButton(guiLeft + 8, guiTop + 196, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.mute_microphone").getVisualOrderText()), mouseX, mouseY);
        });
        addRenderableWidget(mute);

        disable = new ToggleImageButton(guiLeft + 31, guiTop + 196, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.disable_voice_chat").getVisualOrderText()), mouseX, mouseY);
        });
        addRenderableWidget(disable);

        showHUD = new ToggleImageButton(guiLeft + 54, guiTop + 196, GROUP_HUD, VoicechatClient.CLIENT_CONFIG.showGroupHUD::get, button -> {
            VoicechatClient.CLIENT_CONFIG.showGroupHUD.set(!VoicechatClient.CLIENT_CONFIG.showGroupHUD.get()).save();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.show_group_hud").getVisualOrderText()), mouseX, mouseY);
        });
        addRenderableWidget(showHUD);

        leave = new ImageButton(guiLeft + 168, guiTop + 196, LEAVE, button -> {
            CommonCompatibilityManager.INSTANCE.getNetManager().sendToServer(new LeaveGroupPacket());
            minecraft.setScreen(new JoinGroupScreen());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.leave_group").getVisualOrderText()), mouseX, mouseY);
        });
        addRenderableWidget(leave);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
        showHUD.active = !VoicechatClient.CLIENT_CONFIG.hideIcons.get();
    }

    @Override
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        playerList.drawGuiContainerBackgroundLayer(poseStack, delta, mouseX, mouseY);

        playerList.drawGuiContainerForegroundLayer(poseStack, mouseX, mouseY);

        font.draw(poseStack, new TextComponent(group.getName()), guiLeft + 8, guiTop + 5, FONT_COLOR);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (playerList.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (playerList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (playerList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

}
