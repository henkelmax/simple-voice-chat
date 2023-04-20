package de.maxhenkel.voicechat.gui.group;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.gui.GroupType;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class JoinGroupEntry extends ListScreenEntryBase<JoinGroupEntry> {

    protected static final ResourceLocation LOCK = new ResourceLocation(Voicechat.MODID, "textures/icons/lock.png");
    protected static final Component GROUP_MEMBERS = Component.translatable("message.voicechat.group_members").withStyle(ChatFormatting.GRAY);
    protected static final Component NO_GROUP_MEMBERS = Component.translatable("message.voicechat.no_group_members").withStyle(ChatFormatting.GRAY);

    protected static final int SKIN_SIZE = 12;
    protected static final int PADDING = 4;
    protected static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    protected static final int BG_FILL_SELECTED = FastColor.ARGB32.color(255, 90, 90, 90);
    protected static final int PLAYER_NAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);

    protected final ListScreenBase parent;
    protected final Minecraft minecraft;
    protected final Group group;

    public JoinGroupEntry(ListScreenBase parent, Group group) {
        this.parent = parent;
        this.minecraft = Minecraft.getInstance();
        this.group = group;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        if (hovered) {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL_SELECTED);
        } else {
            guiGraphics.fill(left, top, left + width, top + height, BG_FILL);
        }

        boolean hasPassword = group.group.hasPassword();

        if (hasPassword) {
            guiGraphics.blit(LOCK, left + PADDING, top + height / 2 - 8, 0, 0, 16, 16, 16, 16);
        }

        MutableComponent groupName = Component.literal(group.group.getName());
        guiGraphics.drawString(minecraft.font, groupName, left + PADDING + (hasPassword ? 16 + PADDING : 0), top + height / 2 - minecraft.font.lineHeight / 2, PLAYER_NAME_COLOR, false);

        int textWidth = minecraft.font.width(groupName) + (hasPassword ? 16 + PADDING : 0);

        int headsPerRow = (width - (PADDING + textWidth + PADDING + PADDING)) / (SKIN_SIZE + 1);
        int rows = 2;

        for (int i = 0; i < group.members.size(); i++) {
            PlayerState state = group.members.get(i);

            int headXIndex = i / rows;
            int headYIndex = i % rows;

            if (i >= headsPerRow * rows) {
                break;
            }

            int headPosX = left + width - SKIN_SIZE - PADDING - headXIndex * (SKIN_SIZE + 1);
            int headPosY = top + height / 2 - ((SKIN_SIZE * 2 + 2) / 2) + ((SKIN_SIZE * 2 + 2) / 2) * headYIndex;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(headPosX, headPosY, 0);
            float scale = (float) SKIN_SIZE / 8F;
            guiGraphics.pose().scale(scale, scale, scale);
            ResourceLocation skin = GameProfileUtils.getSkin(state.getUuid());
            guiGraphics.blit(skin, 0, 0, 8, 8, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            guiGraphics.blit(skin, 0, 0, 40, 8, 8, 8, 64, 64);
            RenderSystem.disableBlend();
            guiGraphics.pose().popPose();
        }

        if (!hovered) {
            return;
        }
        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        if (group.getGroup().getType().equals(de.maxhenkel.voicechat.api.Group.Type.NORMAL)) {
            tooltip.add(Component.translatable("message.voicechat.group_title", Component.literal(group.getGroup().getName())).getVisualOrderText());
        } else {
            tooltip.add(Component.translatable("message.voicechat.group_type_title", Component.literal(group.getGroup().getName()), GroupType.fromType(group.getGroup().getType()).getTranslation()).getVisualOrderText());
        }

        if (group.getMembers().isEmpty()) {
            tooltip.add(NO_GROUP_MEMBERS.getVisualOrderText());
        } else {
            tooltip.add(GROUP_MEMBERS.getVisualOrderText());
            int maxMembers = 10;
            for (int i = 0; i < group.getMembers().size(); i++) {
                if (i >= maxMembers) {
                    tooltip.add(Component.translatable("message.voicechat.more_members", group.getMembers().size() - maxMembers).withStyle(ChatFormatting.GRAY).getVisualOrderText());
                    break;
                }
                PlayerState state = group.getMembers().get(i);
                tooltip.add(Component.literal("  " + state.getName()).withStyle(ChatFormatting.GRAY).getVisualOrderText());
            }
        }

        parent.postRender(() -> {
            guiGraphics.renderTooltip(minecraft.font, tooltip, mouseX, mouseY);
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
