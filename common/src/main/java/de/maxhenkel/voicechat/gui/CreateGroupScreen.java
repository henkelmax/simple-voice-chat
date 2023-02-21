package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.net.CreateGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class CreateGroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_create_group.png");
    private static final Component TITLE = new TranslatableComponent("gui.voicechat.create_group.title");
    private static final Component CREATE = new TranslatableComponent("message.voicechat.create");
    private static final Component CREATE_GROUP = new TranslatableComponent("message.voicechat.create_group");
    private static final Component GROUP_NAME = new TranslatableComponent("message.voicechat.group_name");
    private static final Component OPTIONAL_PASSWORD = new TranslatableComponent("message.voicechat.optional_password");
    private static final Component GROUP_TYPE = new TranslatableComponent("message.voicechat.group_type");
    private static final Component TYPE_NORMAL = new TranslatableComponent("message.voicechat.group_type.normal");
    private static final Component DESCRIPTION_TYPE_NORMAL = new TranslatableComponent("message.voicechat.group_type.normal.description");
    private static final Component TYPE_OPEN = new TranslatableComponent("message.voicechat.group_type.open");
    private static final Component DESCRIPTION_TYPE_OPEN = new TranslatableComponent("message.voicechat.group_type.open.description");
    private static final Component TYPE_ISOLATED = new TranslatableComponent("message.voicechat.group_type.isolated");
    private static final Component DESCRIPTION_TYPE_ISOLATED = new TranslatableComponent("message.voicechat.group_type.isolated.description");

    private EditBox groupName;
    private EditBox password;
    private GroupType groupType;
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

        groupName = new EditBox(font, guiLeft + 7, guiTop + 32, xSize - 7 * 2, 10, TextComponent.EMPTY);
        groupName.setMaxLength(16);
        groupName.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addRenderableWidget(groupName);

        password = new EditBox(font, guiLeft + 7, guiTop + 58, xSize - 7 * 2, 10, TextComponent.EMPTY);
        password.setMaxLength(16);
        password.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addRenderableWidget(password);

        addRenderableWidget(CycleButton.builder(GroupType::getTranslation).withValues(GroupType.values()).withInitialValue(GroupType.NORMAL).withTooltip(object -> {
            return Tooltip.create(object.getDescription());
        }).create(guiLeft + 6, guiTop + 71, xSize - 12, 20, GROUP_TYPE, (button, type) -> {
            groupType = type;
        }));

        createGroup = new Button(guiLeft + 6, guiTop + ySize - 27, xSize - 12, 20, CREATE, button -> {
            if (!groupName.getValue().isEmpty()) {
                NetManager.sendToServer(new CreateGroupPacket(groupName.getValue(), password.getValue().isEmpty() ? null : password.getValue(), groupType.getType()));
            }
        });
        addRenderableWidget(createGroup);
    }

    private enum GroupType {
        NORMAL(TYPE_NORMAL, DESCRIPTION_TYPE_NORMAL, Group.Type.NORMAL),
        OPEN(TYPE_OPEN, DESCRIPTION_TYPE_OPEN, Group.Type.OPEN),
        ISOLATED(TYPE_ISOLATED, DESCRIPTION_TYPE_ISOLATED, Group.Type.ISOLATED);

        private final Component translation;
        private final Component description;
        private final Group.Type type;

        GroupType(Component translation, Component description, Group.Type type) {
            this.translation = translation;
            this.description = description;
            this.type = type;
        }

        public Component getTranslation() {
            return translation;
        }

        public Component getDescription() {
            return description;
        }

        public Group.Type getType() {
            return type;
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
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
            return true;
        }
        return groupName.keyPressed(keyCode, scanCode, modifiers) | password.keyPressed(keyCode, scanCode, modifiers) | super.keyPressed(keyCode, scanCode, modifiers);
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
