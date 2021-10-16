package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class EnterPasswordScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_enter_password.png");

    private EditBox password;
    private Button joinGroup;
    private ClientGroup group;

    public EnterPasswordScreen(ClientGroup group) {
        super(new TranslatableComponent("gui.voicechat.enter_password.title"), 195, 74);
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        clearWidgets();
        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        password = new EditBox(font, guiLeft + 7, guiTop + 7 + (font.lineHeight + 5) * 2 - 5 + 2, xSize - 7 * 2, 10, TextComponent.EMPTY);
        password.setMaxLength(16);
        password.setFilter(s -> s.isEmpty() || Voicechat.GROUP_REGEX.matcher(s).matches());
        addRenderableWidget(password);

        joinGroup = new Button(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 7 * 2, 20, new TranslatableComponent("message.voicechat.join_group"), button -> {
            if (!password.getValue().isEmpty()) {
                NetManager.sendToServer(new JoinGroupPacket(group.getId(), password.getValue()));
            }
        });
        addRenderableWidget(joinGroup);
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
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        Component title = new TranslatableComponent("message.voicechat.enter_group_password");
        font.draw(poseStack, title, guiLeft + xSize / 2 - font.width(title) / 2, guiTop + 7, FONT_COLOR);
        font.draw(poseStack, new TranslatableComponent("message.voicechat.password"), guiLeft + 8, guiTop + 7 + font.lineHeight + 5, FONT_COLOR);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.setScreen(null);
            return true;
        }
        return password.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String passwordText = password.getValue();
        init(client, width, height);
        password.setValue(passwordText);
    }

}
