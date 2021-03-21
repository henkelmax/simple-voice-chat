package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.GroupList;
import de.maxhenkel.voicechat.gui.widgets.ImageButton;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupScreen extends VoiceChatScreenBase {

    private static final Identifier TEXTURE = new Identifier(Voicechat.MODID, "textures/gui/gui_group.png");
    private static final Identifier LEAVE = new Identifier(Voicechat.MODID, "textures/gui/leave.png");
    private static final Identifier MICROPHONE = new Identifier(Voicechat.MODID, "textures/gui/micrphone_button.png");
    private static final Identifier SPEAKER = new Identifier(Voicechat.MODID, "textures/gui/speaker_button.png");

    private GroupList playerList;
    private ToggleImageButton mute;
    private ToggleImageButton disable;
    private ImageButton leave;

    public GroupScreen() {
        super(new TranslatableText("gui.voicechat.group.title"), 195, 222);
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        buttons.clear();
        children.clear();

        ClientPlayerStateManager stateManager = VoicechatClient.CLIENT.getPlayerStateManager();

        playerList = new GroupList(this, 9, 16, 160, 176, this::getGroupMembers);

        mute = new ToggleImageButton(guiLeft + 8, guiTop + 196, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, (button, matrices, mouseX, mouseY) -> {
            renderOrderedTooltip(matrices, Collections.singletonList(new TranslatableText("message.voicechat.mute_microphone").asOrderedText()), mouseX, mouseY);
        });
        addButton(mute);

        disable = new ToggleImageButton(guiLeft + 31, guiTop + 196, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, (button, matrices, mouseX, mouseY) -> {
            renderOrderedTooltip(matrices, Collections.singletonList(new TranslatableText("message.voicechat.disable_voice_chat").asOrderedText()), mouseX, mouseY);
        });
        addButton(disable);

        leave = new ImageButton(guiLeft + 168, guiTop + 196, LEAVE, button -> {
            VoicechatClient.CLIENT.getPlayerStateManager().setGroup(null);
            client.openScreen(new CreateGroupScreen());
        }, (button, matrices, mouseX, mouseY) -> {
            renderOrderedTooltip(matrices, Collections.singletonList(new TranslatableText("message.voicechat.leave_group").asOrderedText()), mouseX, mouseY);
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
    }

    public List<PlayerState> getGroupMembers() {
        List<PlayerState> entries = new ArrayList<>();
        String group = getGroup();

        for (PlayerState state : VoicechatClient.CLIENT.getPlayerStateManager().getPlayerStates()) {
            if (state.getGroup() != null && state.getGroup().equals(group)) {
                entries.add(state);
            }
        }

        return entries;
    }

    public String getGroup() {
        String group = VoicechatClient.CLIENT.getPlayerStateManager().getGroup();
        if (group == null) {
            return "";
        }
        return group;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        client.getTextureManager().bindTexture(TEXTURE);
        drawTexture(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);

        playerList.drawGuiContainerBackgroundLayer(matrixStack, delta, mouseX, mouseY);

        playerList.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        textRenderer.draw(matrixStack, new LiteralText(getGroup()), guiLeft + 8, guiTop + 5, FONT_COLOR);

        for (AbstractButtonWidget widget : buttons) {
            widget.render(matrixStack, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            client.player.closeScreen();
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
