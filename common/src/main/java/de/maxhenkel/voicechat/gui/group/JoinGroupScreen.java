package de.maxhenkel.voicechat.gui.group;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.EnterPasswordScreen;
import de.maxhenkel.voicechat.gui.widgets.ButtonBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class JoinGroupScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_join_group.png");
    protected static final ITextComponent TITLE = new TextComponentTranslation("gui.voicechat.join_create_group.title");
    protected static final ITextComponent CREATE_GROUP = new TextComponentTranslation("message.voicechat.create_group_button");
    protected static final ITextComponent JOIN_CREATE_GROUP = new TextComponentTranslation("message.voicechat.join_create_group");
    protected static final ITextComponent NO_GROUPS = new TextComponentTranslation("message.voicechat.no_groups").setStyle(new Style().setColor(TextFormatting.GRAY));

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;

    protected JoinGroupList groupList;
    protected ButtonBase createGroup;
    protected int units;

    public JoinGroupScreen() {
        super(TITLE, 236, 0);
    }

    @Override
    public void initGui() {
        super.initGui();
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
        setList(groupList);

        createGroup = new ButtonBase(0, guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 14, 20, CREATE_GROUP) {
            @Override
            public void onPress() {
                mc.displayGuiScreen(new CreateGroupScreen());
            }
        };
        addButton(createGroup);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        groupList.tick();
    }

    @Override
    public void renderBackground(int mouseX, int mouseY, float delta) {
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, HEADER_SIZE);
        for (int i = 0; i < units; i++) {
            drawTexturedModalRect(guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * i, 0, HEADER_SIZE, xSize, UNIT_SIZE);
        }
        drawTexturedModalRect(guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * units, 0, HEADER_SIZE + UNIT_SIZE, xSize, FOOTER_SIZE);
        drawTexturedModalRect(guiLeft + 10, guiTop + HEADER_SIZE + 6 - 2, xSize, 0, 12, 12);
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, float delta) {
        fontRenderer.drawString(JOIN_CREATE_GROUP.getFormattedText(), guiLeft + xSize / 2 - fontRenderer.getStringWidth(JOIN_CREATE_GROUP.getUnformattedComponentText()) / 2, guiTop + 5, FONT_COLOR);

        if (!groupList.isEmpty()) {
            groupList.drawScreen(mouseX, mouseY, delta);
        } else {
            drawCenteredString(fontRenderer, NO_GROUPS.getUnformattedComponentText(), width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 - fontRenderer.FONT_HEIGHT / 2, -1);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        for (JoinGroupEntry entry : groupList.children()) {
            if (entry.isSelected()) {
                ClientGroup group = entry.getGroup().getGroup();
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.F));
                if (group.hasPassword()) {
                    mc.displayGuiScreen(new EnterPasswordScreen(group));
                } else {
                    NetManager.sendToServer(new JoinGroupPacket(group.getId(), null));
                }
                return;
            }
        }
    }

}
