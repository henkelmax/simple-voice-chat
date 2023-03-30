package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

public class EnterPasswordScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_enter_password.png");
    private static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.enter_password.title");
    private static final ITextComponent JOIN_GROUP = new TranslationTextComponent("message.voicechat.join_group");
    private static final ITextComponent ENTER_GROUP_PASSWORD = new TranslationTextComponent("message.voicechat.enter_group_password");
    private static final ITextComponent PASSWORD = new TranslationTextComponent("message.voicechat.password");

    private TextFieldWidget password;
    private Button joinGroup;
    private ClientGroup group;

    public EnterPasswordScreen(ClientGroup group) {
        super(TITLE, 195, 74);
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        children.clear();
        buttons.clear();

        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        password = new TextFieldWidget(font, guiLeft + 7, guiTop + 7 + (font.lineHeight + 5) * 2 - 5 + 2, xSize - 7 * 2, 10, new StringTextComponent(""));
        password.setMaxLength(32);
        password.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addButton(password);

        joinGroup = new Button(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 7 * 2, 20, JOIN_GROUP, button -> {
            joinGroup();
        });
        addButton(joinGroup);
    }

    private void joinGroup() {
        if (!password.getValue().isEmpty()) {
            NetManager.sendToServer(new JoinGroupPacket(group.getId(), password.getValue()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        password.tick();
        joinGroup.active = !password.getValue().isEmpty();
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
        font.draw(poseStack, ENTER_GROUP_PASSWORD, guiLeft + xSize / 2 - font.width(ENTER_GROUP_PASSWORD) / 2, guiTop + 7, FONT_COLOR);
        font.draw(poseStack, PASSWORD, guiLeft + 8, guiTop + 7 + font.lineHeight + 5, FONT_COLOR);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
            return true;
        }
        if (password.keyPressed(keyCode, scanCode, modifiers) | super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            joinGroup();
            return true;
        }
        return false;
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String passwordText = password.getValue();
        init(client, width, height);
        password.setValue(passwordText);
    }

}
