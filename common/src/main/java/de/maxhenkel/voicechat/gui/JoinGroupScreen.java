package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.JoinGroupList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class JoinGroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_join_group.png");
    private static final Component TITLE = new TranslatableComponent("gui.voicechat.join_create_group.title");
    private static final Component CREATE_GROUP = new TranslatableComponent("message.voicechat.create_group_button");
    private static final Component JOIN_CREATE_GROUP = new TranslatableComponent("message.voicechat.join_create_group");
    private static final Component JOIN_GROUP = new TranslatableComponent("message.voicechat.join_a_group");

    private JoinGroupList groupList;
    private Button createGroup;

    public JoinGroupScreen() {
        super(TITLE, 195, 153);
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        clearWidgets();
        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        groupList = new JoinGroupList(this, 8, 32, 160, 88);

        createGroup = new Button(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 14, 20, CREATE_GROUP, button -> {
            minecraft.setScreen(new CreateGroupScreen());
        });
        addRenderableWidget(createGroup);
    }

    @Override
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        groupList.drawGuiContainerBackgroundLayer(poseStack, delta, mouseX, mouseY);

        groupList.drawGuiContainerForegroundLayer(poseStack, mouseX, mouseY);

        font.draw(poseStack, JOIN_CREATE_GROUP, guiLeft + xSize / 2 - font.width(JOIN_CREATE_GROUP) / 2, guiTop + 7, FONT_COLOR);
        font.draw(poseStack, JOIN_GROUP, guiLeft + 7, guiTop + 7 + font.lineHeight + 5, FONT_COLOR);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (groupList.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (groupList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (groupList.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

}
