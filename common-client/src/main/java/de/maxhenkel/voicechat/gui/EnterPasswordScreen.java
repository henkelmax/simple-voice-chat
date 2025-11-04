package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.ClientServerNetManager;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class EnterPasswordScreen extends VoiceChatScreenBase {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/gui/gui_enter_password.png");
    private static final Component TITLE = Component.translatable("gui.voicechat.enter_password.title");
    private static final Component JOIN_GROUP = Component.translatable("message.voicechat.join_group");
    private static final Component ENTER_GROUP_PASSWORD = Component.translatable("message.voicechat.enter_group_password");
    private static final Component PASSWORD = Component.translatable("message.voicechat.password");

    private EditBox password;
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
        clearWidgets();

        password = new EditBox(font, guiLeft + 7, guiTop + 7 + (font.lineHeight + 5) * 2 - 5, xSize - 7 * 2, 14, Component.empty());
        password.setMaxLength(32);
        password.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addRenderableWidget(password);

        joinGroup = Button.builder(JOIN_GROUP, button -> {
            joinGroup();
        }).bounds(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 7 * 2, 20).build();
        addRenderableWidget(joinGroup);
    }

    private void joinGroup() {
        if (!password.getValue().isEmpty()) {
            ClientServerNetManager.sendToServer(new JoinGroupPacket(group.getId(), password.getValue()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        joinGroup.active = !password.getValue().isEmpty();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawString(font, ENTER_GROUP_PASSWORD, guiLeft + xSize / 2 - font.width(ENTER_GROUP_PASSWORD) / 2, guiTop + 7, FONT_COLOR, false);
        guiGraphics.drawString(font, PASSWORD, guiLeft + 8, guiTop + 7 + font.lineHeight + 5, FONT_COLOR, false);
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
            joinGroup();
            return true;
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        String passwordText = password.getValue();
        init(width, height);
        password.setValue(passwordText);
    }

}
