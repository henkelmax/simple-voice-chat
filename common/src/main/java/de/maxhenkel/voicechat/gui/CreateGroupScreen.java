package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.CreateGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
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
    private static final ITextComponent GROUP_TYPE = new TranslationTextComponent("message.voicechat.group_type");

    private TextFieldWidget groupName;
    private TextFieldWidget password;
    private GroupType groupType;
    private Button groupTypeButton;
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

        groupTypeButton = new Button(guiLeft + 6, guiTop + 71, xSize - 12, 20, GROUP_TYPE, (button) -> {
            groupType = GroupType.values()[(groupType.ordinal() + 1) % GroupType.values().length];
        }) {
            public ITextComponent getMessage() {
                return new TranslationTextComponent("message.voicechat.group_type").append(": ").append(groupType.getTranslation());
            }
        };
        addButton(groupTypeButton);

        createGroup = new Button(guiLeft + 6, guiTop + ySize - 27, xSize - 12, 20, CREATE, button -> {
            createGroup();
        });
        addButton(createGroup);
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
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bind(TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        font.draw(poseStack, CREATE_GROUP, guiLeft + xSize / 2 - font.width(CREATE_GROUP) / 2, guiTop + 7, FONT_COLOR);
        font.draw(poseStack, GROUP_NAME, guiLeft + 8, guiTop + 7 + font.lineHeight + 5, FONT_COLOR);
        font.draw(poseStack, OPTIONAL_PASSWORD, guiLeft + 8, guiTop + 7 + (font.lineHeight + 5) * 2 + 10 + 2, FONT_COLOR);

        if (mouseX >= groupTypeButton.x && mouseY >= groupTypeButton.y && mouseX < groupTypeButton.x + groupTypeButton.getWidth() && mouseY < groupTypeButton.y + groupTypeButton.getHeight()) {
            renderTooltip(poseStack, minecraft.font.split(groupType.getDescription(), 200), mouseX, mouseY);
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
