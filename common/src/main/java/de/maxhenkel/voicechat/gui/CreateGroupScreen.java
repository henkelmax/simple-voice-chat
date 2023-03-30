package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.CreateGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class CreateGroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_create_group.png");
    private static final Component TITLE = Component.translatable("gui.voicechat.create_group.title");
    private static final Component CREATE = Component.translatable("message.voicechat.create");
    private static final Component CREATE_GROUP = Component.translatable("message.voicechat.create_group");
    private static final Component GROUP_NAME = Component.translatable("message.voicechat.group_name");
    private static final Component OPTIONAL_PASSWORD = Component.translatable("message.voicechat.optional_password");
    private static final Component GROUP_TYPE = Component.translatable("message.voicechat.group_type");

    private EditBox groupName;
    private EditBox password;
    private GroupType groupType;
    private CycleButton<GroupType> groupTypeButton;
    private Button createGroup;

    public CreateGroupScreen() {
        super(TITLE, 195, 124);
        groupType = GroupType.NORMAL;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        clearWidgets();
        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        groupName = new EditBox(font, guiLeft + 7, guiTop + 32, xSize - 7 * 2, 10, Component.empty());
        groupName.setMaxLength(24);
        groupName.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addRenderableWidget(groupName);

        password = new EditBox(font, guiLeft + 7, guiTop + 58, xSize - 7 * 2, 10, Component.empty());
        password.setMaxLength(32);
        password.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addRenderableWidget(password);

        groupTypeButton = CycleButton.builder(GroupType::getTranslation).withValues(GroupType.values()).withInitialValue(GroupType.NORMAL).withTooltip(object -> {
            return minecraft.font.split(object.getDescription(), 200);
        }).create(guiLeft + 6, guiTop + 71, xSize - 12, 20, GROUP_TYPE, (button, type) -> {
            groupType = type;
        });
        addRenderableWidget(groupTypeButton);

        createGroup = new Button(guiLeft + 6, guiTop + ySize - 27, xSize - 12, 20, CREATE, button -> {
            createGroup();
        });
        addRenderableWidget(createGroup);
    }

    private void createGroup() {
        if (!groupName.getValue().isEmpty()) {
            NetManager.sendToServer(new CreateGroupPacket(groupName.getValue(), password.getValue().isEmpty() ? null : password.getValue(), groupType.getType()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        groupName.tick();
        password.tick();
        createGroup.active = !groupName.getValue().isEmpty();
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        font.draw(poseStack, CREATE_GROUP, guiLeft + xSize / 2 - font.width(CREATE_GROUP) / 2, guiTop + 7, FONT_COLOR);
        font.draw(poseStack, GROUP_NAME, guiLeft + 8, guiTop + 7 + font.lineHeight + 5, FONT_COLOR);
        font.draw(poseStack, OPTIONAL_PASSWORD, guiLeft + 8, guiTop + 7 + (font.lineHeight + 5) * 2 + 10 + 2, FONT_COLOR);

        if (mouseX >= groupTypeButton.x && mouseY >= groupTypeButton.y && mouseX < groupTypeButton.x + groupTypeButton.getWidth() && mouseY < groupTypeButton.y + groupTypeButton.getHeight()) {
            renderTooltip(poseStack, groupTypeButton.getTooltip(), mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
            return true;
        }
        if (groupName.keyPressed(keyCode, scanCode, modifiers) | password.keyPressed(keyCode, scanCode, modifiers) | super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            createGroup();
            return true;
        }
        return false;
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String groupNameText = groupName.getValue();
        String passwordText = password.getValue();
        init(client, width, height);
        groupName.setValue(groupNameText);
        password.setValue(passwordText);
    }

}
