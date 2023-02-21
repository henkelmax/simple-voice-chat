package de.maxhenkel.voicechat.gui.group;

import com.google.common.collect.Lists;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;

import java.util.ArrayList;
import java.util.List;

public class JoinGroupEntry extends ListScreenEntryBase {

    protected static final ResourceLocation LOCK = new ResourceLocation(Voicechat.MODID, "textures/icons/lock.png");
    protected static final ITextComponent GROUP_MEMBERS = new TextComponentTranslation("message.voicechat.group_members").setStyle(new Style().setColor(TextFormatting.WHITE));
    protected static final ITextComponent NO_GROUP_MEMBERS = new TextComponentTranslation("message.voicechat.no_group_members").setStyle(new Style().setColor(TextFormatting.WHITE));

    protected static final int SKIN_SIZE = 12;
    protected static final int PADDING = 4;
    protected static final int BG_FILL = VoiceChatScreenBase.color(255, 74, 74, 74);
    protected static final int BG_FILL_SELECTED = VoiceChatScreenBase.color(255, 90, 90, 90);
    protected static final int PLAYER_NAME_COLOR = VoiceChatScreenBase.color(255, 255, 255, 255);

    protected final ListScreenBase parent;
    protected final Minecraft minecraft;
    protected final Group group;

    public JoinGroupEntry(ListScreenBase parent, Group group) {
        this.parent = parent;
        this.minecraft = Minecraft.getMinecraft();
        this.group = group;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        if (isSelected) {
            GuiScreen.drawRect(x, y, x + listWidth, y + slotHeight, BG_FILL_SELECTED);
        } else {
            GuiScreen.drawRect(x, y, x + listWidth, y + slotHeight, BG_FILL);
        }

        boolean hasPassword = group.group.hasPassword();

        if (hasPassword) {
            GlStateManager.color(1F, 1F, 1F, 1F);
            minecraft.getTextureManager().bindTexture(LOCK);
            GuiScreen.drawModalRectWithCustomSizedTexture(x + PADDING, y + slotHeight / 2 - 8, 0, 0, 16, 16, 16, 16);
        }

        minecraft.fontRenderer.drawString(group.group.getName(), x + PADDING + (hasPassword ? 16 + PADDING : 0), y + slotHeight / 2 - minecraft.fontRenderer.FONT_HEIGHT / 2, PLAYER_NAME_COLOR);

        int textWidth = minecraft.fontRenderer.getStringWidth(group.group.getName()) + (hasPassword ? 16 + PADDING : 0);

        int headsPerRow = (listWidth - (PADDING + textWidth + PADDING + PADDING)) / (SKIN_SIZE + 1);
        int rows = 2;

        for (int i = 0; i < group.members.size(); i++) {
            PlayerState state = group.members.get(i);

            int headXIndex = i / rows;
            int headYIndex = i % rows;

            if (i >= headsPerRow * rows) {
                break;
            }

            int headPosX = x + listWidth - SKIN_SIZE - PADDING - headXIndex * (SKIN_SIZE + 1);
            int headPosY = y + slotHeight / 2 - ((SKIN_SIZE * 2 + 2) / 2) + ((SKIN_SIZE * 2 + 2) / 2) * headYIndex;

            GlStateManager.pushMatrix();
            GlStateManager.color(1F, 1F, 1F, 1F);
            minecraft.getTextureManager().bindTexture(GameProfileUtils.getSkin(state.getUuid()));
            GlStateManager.translate(headPosX, headPosY, 0);
            float scale = (float) SKIN_SIZE / 8F;
            GlStateManager.scale(scale, scale, scale);
            GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 8, 8, 8, 8, 64, 64);
            GlStateManager.enableBlend();
            GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 40, 8, 8, 8, 64, 64);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        if (!isSelected) {
            return;
        }
        List<String> tooltip = Lists.newArrayList();
        if (group.getMembers().isEmpty()) {
            tooltip.add(NO_GROUP_MEMBERS.getUnformattedComponentText());
        } else {
            tooltip.add(GROUP_MEMBERS.getUnformattedComponentText());
            int maxMembers = 10;
            for (int i = 0; i < group.getMembers().size(); i++) {
                if (i >= maxMembers) {
                    tooltip.add(new TextComponentTranslation("message.voicechat.more_members", group.getMembers().size() - maxMembers).setStyle(new Style().setColor(TextFormatting.GRAY)).getFormattedText());
                    break;
                }
                PlayerState state = group.getMembers().get(i);
                tooltip.add(new TextComponentString("  " + state.getName()).setStyle(new Style().setColor(TextFormatting.GRAY)).getFormattedText());
            }
        }

        parent.postRender(() -> {
            parent.drawHoveringText(tooltip, mouseX, mouseY);
        });
    }

    public Group getGroup() {
        return group;
    }

    public static class Group {
        private final ClientGroup group;
        private final List<PlayerState> members;

        public Group(ClientGroup group) {
            this.group = group;
            this.members = new ArrayList<>();
        }

        public ClientGroup getGroup() {
            return group;
        }

        public List<PlayerState> getMembers() {
            return members;
        }
    }

}
