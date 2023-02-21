package de.maxhenkel.voicechat.gui;

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
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class CreateGroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_create_group.png");
    private static final ITextComponent TITLE = new TextComponentTranslation("gui.voicechat.create_group.title");
    private static final ITextComponent CREATE = new TextComponentTranslation("message.voicechat.create");
    private static final ITextComponent CREATE_GROUP = new TextComponentTranslation("message.voicechat.create_group");
    private static final ITextComponent GROUP_NAME = new TextComponentTranslation("message.voicechat.group_name");
    private static final ITextComponent OPTIONAL_PASSWORD = new TextComponentTranslation("message.voicechat.optional_password");
    private static final Component GROUP_TYPE = new TranslatableComponent("message.voicechat.group_type");
    private static final Component TYPE_NORMAL = new TranslatableComponent("message.voicechat.group_type.normal");
    private static final Component DESCRIPTION_TYPE_NORMAL = new TranslatableComponent("message.voicechat.group_type.normal.description");
    private static final Component TYPE_OPEN = new TranslatableComponent("message.voicechat.group_type.open");
    private static final Component DESCRIPTION_TYPE_OPEN = new TranslatableComponent("message.voicechat.group_type.open.description");
    private static final Component TYPE_ISOLATED = new TranslatableComponent("message.voicechat.group_type.isolated");
    private static final Component DESCRIPTION_TYPE_ISOLATED = new TranslatableComponent("message.voicechat.group_type.isolated.description");

    private GuiTextField groupName;
    private GuiTextField password;
    private GroupType groupType;
    private ButtonBase createGroup;

    public CreateGroupScreen() {
        super(TITLE, 195, 124);
        groupType = GroupType.NORMAL;
    }

    @Override
    public void initGui() {
        super.initGui();
        hoverAreas.clear();
        hoverAreas.clear();
        buttonList.clear();

        Keyboard.enableRepeatEvents(true);

        groupName = new GuiTextField(0, fontRenderer, guiLeft + 7, guiTop + 32, xSize - 7 * 2, 10);
        groupName.setMaxStringLength(16);
        groupName.setValidator(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());

        password = new GuiTextField(1, fontRenderer, guiLeft + 7, guiTop + 58, xSize - 7 * 2, 10);
        password.setMaxStringLength(16);
        password.setValidator(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());

        addRenderableWidget(CycleButton.builder(GroupType::getTranslation).withValues(GroupType.values()).withInitialValue(GroupType.NORMAL).withTooltip(object -> {
            return Tooltip.create(object.getDescription());
        }).create(guiLeft + 6, guiTop + 71, xSize - 12, 20, GROUP_TYPE, (button, type) -> {
            groupType = type;
        }));

        createGroup = new ButtonBase(2, guiLeft + 6, guiTop + ySize - 27, xSize - 12, 20, CREATE) {
            @Override
            public void onPress() {
                if (!groupName.getText().isEmpty()) {
                    NetManager.sendToServer(new CreateGroupPacket(groupName.getText(), password.getText().isEmpty() ? null : password.getText()));
                }
            }
        };
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
    public void updateScreen() {
        super.updateScreen();
        groupName.updateCursorCounter();
        password.updateCursorCounter();
        createGroup.enabled = !groupName.getText().isEmpty();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void renderBackground(int mouseX, int mouseY, float delta) {
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, float delta) {
        groupName.drawTextBox();
        password.drawTextBox();
        fontRenderer.drawString(CREATE_GROUP.getUnformattedComponentText(), guiLeft + xSize / 2 - fontRenderer.getStringWidth(CREATE_GROUP.getUnformattedComponentText()) / 2, guiTop + 7, FONT_COLOR);
        fontRenderer.drawString(GROUP_NAME.getUnformattedComponentText(), guiLeft + 8, guiTop + 7 + fontRenderer.FONT_HEIGHT + 5, FONT_COLOR);
        fontRenderer.drawString(OPTIONAL_PASSWORD.getUnformattedComponentText(), guiLeft + 8, guiTop + 7 + (fontRenderer.FONT_HEIGHT + 5) * 2 + 10 + 2, FONT_COLOR);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        groupName.textboxKeyTyped(typedChar, keyCode);
        password.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        groupName.mouseClicked(mouseX, mouseY, mouseButton);
        password.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onResize(Minecraft minecraft, int width, int height) {
        String groupNameText = groupName.getText();
        String passwordText = password.getText();
        super.onResize(minecraft, width, height);
        groupName.setText(groupNameText);
        password.setText(passwordText);
    }

}
