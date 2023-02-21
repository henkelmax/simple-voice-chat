package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.net.CreateGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

public class CreateGroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_create_group.png");
    private static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.create_group.title");
    private static final ITextComponent CREATE = new TranslationTextComponent("message.voicechat.create");
    private static final ITextComponent CREATE_GROUP = new TranslationTextComponent("message.voicechat.create_group");
    private static final ITextComponent GROUP_NAME = new TranslationTextComponent("message.voicechat.group_name");
    private static final ITextComponent OPTIONAL_PASSWORD = new TranslationTextComponent("message.voicechat.optional_password");
    private static final Component GROUP_TYPE = new TranslatableComponent("message.voicechat.group_type");
    private static final Component TYPE_NORMAL = new TranslatableComponent("message.voicechat.group_type.normal");
    private static final Component DESCRIPTION_TYPE_NORMAL = new TranslatableComponent("message.voicechat.group_type.normal.description");
    private static final Component TYPE_OPEN = new TranslatableComponent("message.voicechat.group_type.open");
    private static final Component DESCRIPTION_TYPE_OPEN = new TranslatableComponent("message.voicechat.group_type.open.description");
    private static final Component TYPE_ISOLATED = new TranslatableComponent("message.voicechat.group_type.isolated");
    private static final Component DESCRIPTION_TYPE_ISOLATED = new TranslatableComponent("message.voicechat.group_type.isolated.description");

    private TextFieldWidget groupName;
    private TextFieldWidget password;
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
        children.clear();
        buttons.clear();

        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        groupName = new TextFieldWidget(font, guiLeft + 7, guiTop + 32, xSize - 7 * 2, 10, new StringTextComponent(""));
        groupName.setMaxLength(16);
        groupName.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addButton(groupName);

        password = new TextFieldWidget(font, guiLeft + 7, guiTop + 58, xSize - 7 * 2, 10, new StringTextComponent(""));
        password.setMaxLength(16);
        password.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addButton(password);

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
        addButton(createGroup);
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
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bind(TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
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
