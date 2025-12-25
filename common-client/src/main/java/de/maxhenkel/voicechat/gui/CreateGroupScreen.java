package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.ClientServerNetManager;
import de.maxhenkel.voicechat.net.CreateGroupPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CreateGroupScreen extends VoiceChatScreenBase {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/gui/gui_create_group.png");
    private static final Component TITLE = Component.translatable("gui.voicechat.create_group.title");
    private static final Component CREATE = Component.translatable("message.voicechat.create");
    private static final Component CREATE_GROUP = Component.translatable("message.voicechat.create_group");
    private static final Component GROUP_NAME = Component.translatable("message.voicechat.group_name");
    private static final Component OPTIONAL_PASSWORD = Component.translatable("message.voicechat.optional_password");
    private static final Component GROUP_TYPE = Component.translatable("message.voicechat.group_type");

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

        groupName = new EditBox(font, guiLeft + 7, guiTop + 30, xSize - 7 * 2, 14, Component.empty());
        groupName.setMaxLength(Voicechat.MAX_GROUP_NAME_LENGTH);
        groupName.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addRenderableWidget(groupName);

        password = new EditBox(font, guiLeft + 7, guiTop + 56, xSize - 7 * 2, 14, Component.empty());
        password.setMaxLength(Voicechat.MAX_GROUP_NAME_LENGTH);
        password.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addRenderableWidget(password);

        addRenderableWidget(CycleButton.builder(GroupType::getTranslation, GroupType.NORMAL).withValues(GroupType.values()).withTooltip(object -> {
            return Tooltip.create(object.getDescription());
        }).create(guiLeft + 6, guiTop + 74, xSize - 12, 20, GROUP_TYPE, (button, type) -> {
            groupType = type;
        }));

        createGroup = Button.builder(CREATE, button -> {
            createGroup();
        }).bounds(guiLeft + 6, guiTop + ySize - 27, xSize - 12, 20).build();
        addRenderableWidget(createGroup);
    }

    private void createGroup() {
        if (!groupName.getValue().isEmpty()) {
            ClientServerNetManager.sendToServer(new CreateGroupPacket(groupName.getValue(), password.getValue().isEmpty() ? null : password.getValue(), groupType.getType()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        createGroup.active = !groupName.getValue().isEmpty();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawString(font, CREATE_GROUP, guiLeft + xSize / 2 - font.width(CREATE_GROUP) / 2, guiTop + 7, FONT_COLOR, false);
        guiGraphics.drawString(font, GROUP_NAME, guiLeft + 8, guiTop + 7 + font.lineHeight + 5, FONT_COLOR, false);
        guiGraphics.drawString(font, OPTIONAL_PASSWORD, guiLeft + 8, guiTop + 7 + (font.lineHeight + 5) * 2 + 10 + 2, FONT_COLOR, false);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.isEscape()) {
            minecraft.setScreen(null);
            return true;
        }
        if (super.keyPressed(keyEvent)) {
            return true;
        }
        if (keyEvent.isConfirmation()) {
            createGroup();
            return true;
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        String groupNameText = groupName.getValue();
        String passwordText = password.getValue();
        init(width, height);
        groupName.setValue(groupNameText);
        password.setValue(passwordText);
    }
}
