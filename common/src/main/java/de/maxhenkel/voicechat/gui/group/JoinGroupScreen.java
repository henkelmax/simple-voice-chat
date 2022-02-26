package de.maxhenkel.voicechat.gui.group;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.EnterPasswordScreen;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class JoinGroupScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_join_group.png");
    protected static final ITextComponent TITLE = new TranslationTextComponent("gui.voicechat.join_create_group.title");
    protected static final ITextComponent CREATE_GROUP = new TranslationTextComponent("message.voicechat.create_group_button");
    protected static final ITextComponent JOIN_CREATE_GROUP = new TranslationTextComponent("message.voicechat.join_create_group");
    protected static final ITextComponent NO_GROUPS = new TranslationTextComponent("message.voicechat.no_groups").withStyle(TextFormatting.GRAY);

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;

    protected JoinGroupList groupList;
    protected Button createGroup;
    protected int units;

    public JoinGroupScreen() {
        super(TITLE, 236, 0);
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = MathHelper.ceil((float) (CELL_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        if (groupList != null) {
            groupList.updateSize(width, height, guiTop + HEADER_SIZE, guiTop + HEADER_SIZE + units * UNIT_SIZE);
        } else {
            groupList = new JoinGroupList(this, width, height, guiTop + HEADER_SIZE, guiTop + HEADER_SIZE + units * UNIT_SIZE, CELL_HEIGHT);
        }
        addWidget(groupList);

        createGroup = new Button(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 14, 20, CREATE_GROUP, button -> {
            minecraft.setScreen(new CreateGroupScreen());
        });
        addButton(createGroup);
    }

    @Override
    public void tick() {
        super.tick();
        groupList.tick();
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        minecraft.getTextureManager().bind(TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, HEADER_SIZE);
        for (int i = 0; i < units; i++) {
            blit(poseStack, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * i, 0, HEADER_SIZE, xSize, UNIT_SIZE);
        }
        blit(poseStack, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * units, 0, HEADER_SIZE + UNIT_SIZE, xSize, FOOTER_SIZE);
        blit(poseStack, guiLeft + 10, guiTop + HEADER_SIZE + 6 - 2, xSize, 0, 12, 12);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY, float delta) {
        font.draw(poseStack, JOIN_CREATE_GROUP, guiLeft + xSize / 2 - font.width(JOIN_CREATE_GROUP) / 2, guiTop + 5, FONT_COLOR);

        if (!groupList.isEmpty()) {
            groupList.render(poseStack, mouseX, mouseY, delta);
        } else {
            drawCenteredString(poseStack, font, NO_GROUPS, width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 - font.lineHeight / 2, -1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (JoinGroupEntry entry : groupList.children()) {
            if (entry.isMouseOver(mouseX, mouseY)) {
                ClientGroup group = entry.getGroup().getGroup();
                minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                if (group.hasPassword()) {
                    minecraft.setScreen(new EnterPasswordScreen(group));
                } else {
                    NetManager.sendToServer(new JoinGroupPacket(group.getId(), null));
                }
                return true;
            }
        }
        return false;
    }

}
