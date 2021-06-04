package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.GroupList;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.GroupChatManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;

public class GroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_group.png");
    private static final ResourceLocation LEAVE = new ResourceLocation(Voicechat.MODID, "textures/gui/leave.png");
    private static final ResourceLocation MICROPHONE = new ResourceLocation(Voicechat.MODID, "textures/gui/microphone_button.png");
    private static final ResourceLocation SPEAKER = new ResourceLocation(Voicechat.MODID, "textures/gui/speaker_button.png");
    private static final ResourceLocation GROUP_HUD = new ResourceLocation(Voicechat.MODID, "textures/gui/group_hud_button.png");

    private GroupList playerList;
    private ToggleImageButton mute;
    private ToggleImageButton disable;
    private ToggleImageButton showHUD;
    private ImageButton leave;

    public GroupScreen() {
        super(new TranslatableComponent("gui.voicechat.group.title"), 195, 222);
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        buttons.clear();
        children.clear();

        ClientPlayerStateManager stateManager = VoicechatClient.CLIENT.getPlayerStateManager();

        playerList = new GroupList(this, 9, 16, 160, 176, GroupChatManager::getGroupMembers);

        mute = new ToggleImageButton(guiLeft + 8, guiTop + 196, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.mute_microphone").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(mute);

        disable = new ToggleImageButton(guiLeft + 31, guiTop + 196, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.disable_voice_chat").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(disable);

        showHUD = new ToggleImageButton(guiLeft + 54, guiTop + 196, GROUP_HUD, VoicechatClient.CLIENT_CONFIG.showGroupHUD::get, button -> {
            VoicechatClient.CLIENT_CONFIG.showGroupHUD.set(!VoicechatClient.CLIENT_CONFIG.showGroupHUD.get());
            VoicechatClient.CLIENT_CONFIG.showGroupHUD.save();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.show_group_hud").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(showHUD);

        leave = new ImageButton(guiLeft + 168, guiTop + 196, LEAVE, button -> {
            VoicechatClient.CLIENT.getPlayerStateManager().setGroup(null);
            minecraft.setScreen(new CreateGroupScreen());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslatableComponent("message.voicechat.leave_group").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(leave);

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
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float delta) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);

        playerList.drawGuiContainerBackgroundLayer(matrixStack, delta, mouseX, mouseY);

        playerList.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        font.draw(matrixStack, new TextComponent(GroupChatManager.getGroup()), guiLeft + 8, guiTop + 5, FONT_COLOR);

        for (AbstractWidget widget : buttons) {
            widget.render(matrixStack, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
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
