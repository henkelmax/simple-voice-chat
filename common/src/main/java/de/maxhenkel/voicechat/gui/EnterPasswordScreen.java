package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.ButtonBase;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class EnterPasswordScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_enter_password.png");
    private static final ITextComponent TITLE = new TextComponentTranslation("gui.voicechat.enter_password.title");
    private static final ITextComponent JOIN_GROUP = new TextComponentTranslation("message.voicechat.join_group");
    private static final ITextComponent ENTER_GROUP_PASSWORD = new TextComponentTranslation("message.voicechat.enter_group_password");
    private static final ITextComponent PASSWORD = new TextComponentTranslation("message.voicechat.password");

    private GuiTextField password;
    private ButtonBase joinGroup;
    private ClientGroup group;

    public EnterPasswordScreen(ClientGroup group) {
        super(TITLE, 195, 74);
        this.group = group;
    }

    @Override
    public void initGui() {
        super.initGui();
        hoverAreas.clear();
        hoverAreas.clear();
        buttonList.clear();

        Keyboard.enableRepeatEvents(true);

        password = new GuiTextField(0, fontRenderer, guiLeft + 7, guiTop + 7 + (fontRenderer.FONT_HEIGHT + 5) * 2 - 5 + 2, xSize - 7 * 2, 10);
        password.setMaxStringLength(32);
        password.setValidator(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());

        joinGroup = new ButtonBase(1, guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 7 * 2, 20, JOIN_GROUP) {
            @Override
            public void onPress() {
                joinGroup();
            }
        };
        addButton(joinGroup);
    }

    private void joinGroup() {
        if (!password.getText().isEmpty()) {
            NetManager.sendToServer(new JoinGroupPacket(group.getId(), password.getText()));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (password == null) {
            return;
        }
        password.updateCursorCounter();
        joinGroup.enabled = !password.getText().isEmpty();
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
        if (password != null) {
            password.drawTextBox();
        }
        fontRenderer.drawString(ENTER_GROUP_PASSWORD.getUnformattedComponentText(), guiLeft + xSize / 2 - fontRenderer.getStringWidth(ENTER_GROUP_PASSWORD.getUnformattedComponentText()) / 2, guiTop + 7, FONT_COLOR);
        fontRenderer.drawString(PASSWORD.getUnformattedComponentText(), guiLeft + 8, guiTop + 7 + fontRenderer.FONT_HEIGHT + 5, FONT_COLOR);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (password == null) {
            return;
        }
        if (password.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN) {
            joinGroup();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (password == null) {
            return;
        }
        password.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onResize(Minecraft minecraft, int width, int height) {
        String passwordText = password.getText();
        super.onResize(minecraft, width, height);
        password.setText(passwordText);
    }
}
