package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.widgets.JoinGroupList;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class JoinGroupScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_join_group.png");

    private JoinGroupList groupList;
    private Button createGroup;

    public JoinGroupScreen() {
        super(new TranslationTextComponent("gui.voicechat.join_create_group.title"), 195, 153);
    }

    @Override
    protected void init() {
        super.init();
        hoverAreas.clear();
        buttons.clear();
        children.clear();
        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        groupList = new JoinGroupList(this, 8, 32, 160, 88);

        createGroup = new Button(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 14, 20, new TranslationTextComponent("message.voicechat.create_group_button"), button -> {
            minecraft.setScreen(new CreateGroupScreen());
        });
        addButton(createGroup);
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bind(TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        groupList.drawGuiContainerBackgroundLayer(poseStack, delta, mouseX, mouseY);

        groupList.drawGuiContainerForegroundLayer(poseStack, mouseX, mouseY);

        ITextComponent title = new TranslationTextComponent("message.voicechat.join_create_group");
        font.draw(poseStack, title, guiLeft + xSize / 2 - font.width(title) / 2, guiTop + 7, FONT_COLOR);
        font.draw(poseStack, new TranslationTextComponent("message.voicechat.join_a_group"), guiLeft + 7, guiTop + 7 + font.lineHeight + 5, FONT_COLOR);
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
