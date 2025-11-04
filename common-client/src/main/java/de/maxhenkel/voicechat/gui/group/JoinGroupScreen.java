package de.maxhenkel.voicechat.gui.group;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.CreateGroupScreen;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class JoinGroupScreen extends ListScreenBase {

    protected static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/gui/gui_join_group.png");
    protected static final Component TITLE = Component.translatable("gui.voicechat.join_create_group.title");
    protected static final Component CREATE_GROUP = Component.translatable("message.voicechat.create_group_button");
    protected static final Component JOIN_CREATE_GROUP = Component.translatable("message.voicechat.join_create_group");
    protected static final Component NO_GROUPS = Component.translatable("message.voicechat.no_groups").withStyle(ChatFormatting.GRAY);

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
        int minUnits = Mth.ceil((float) (CELL_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        if (groupList != null) {
            groupList.updateSize(width, units * UNIT_SIZE, 0, guiTop + HEADER_SIZE);
        } else {
            groupList = new JoinGroupList(this, width, units * UNIT_SIZE, guiTop + HEADER_SIZE, CELL_HEIGHT);
        }
        addWidget(groupList);

        createGroup = Button.builder(CREATE_GROUP, button -> {
            minecraft.setScreen(new CreateGroupScreen());
        }).bounds(guiLeft + 7, guiTop + ySize - 20 - 7, xSize - 14, 20).build();
        addRenderableWidget(createGroup);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, 0, 0, xSize, HEADER_SIZE, 256, 256);
        for (int i = 0; i < units; i++) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * i, 0, HEADER_SIZE, xSize, UNIT_SIZE, 256, 256);
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * units, 0, HEADER_SIZE + UNIT_SIZE, xSize, FOOTER_SIZE, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft + 10, guiTop + HEADER_SIZE + 6 - 2, xSize, 0, 12, 12, 256, 256);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawString(font, JOIN_CREATE_GROUP, guiLeft + xSize / 2 - font.width(JOIN_CREATE_GROUP) / 2, guiTop + 5, FONT_COLOR, false);
        if (!groupList.isEmpty()) {
            groupList.render(guiGraphics, mouseX, mouseY, delta);
        } else {
            guiGraphics.drawCenteredString(font, NO_GROUPS, width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 - font.lineHeight / 2, -1);
        }
    }

}
