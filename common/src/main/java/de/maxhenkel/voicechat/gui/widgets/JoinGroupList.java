package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.EnterPasswordScreen;
import de.maxhenkel.voicechat.gui.SkinUtils;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.net.JoinGroupPacket;
import de.maxhenkel.voicechat.net.NetManager;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.ClientGroup;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class JoinGroupList extends WidgetBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_join_group.png");
    private static final ResourceLocation LOCK = new ResourceLocation(Voicechat.MODID, "textures/icons/lock.png");

    protected int offset;
    private final VoiceChatScreenBase.HoverArea[] hoverAreas;
    private final int columnHeight;
    private final int columnCount;

    public JoinGroupList(VoiceChatScreenBase screen, int posX, int posY, int xSize, int ySize) {
        super(screen, posX, posY, xSize, ySize);
        columnHeight = 22;
        columnCount = 4;

        hoverAreas = new VoiceChatScreenBase.HoverArea[columnCount];
        for (int i = 0; i < hoverAreas.length; i++) {
            hoverAreas[i] = new VoiceChatScreenBase.HoverArea(0, i * columnHeight, xSize, columnHeight);
        }
    }

    public List<Group> getGroups() {
        Map<UUID, Group> groups = new HashMap<>();
        Collection<PlayerState> playerStates = ClientManager.getPlayerStateManager().getPlayerStates(true);

        for (PlayerState state : playerStates) {
            if (!state.hasGroup()) {
                continue;
            }

            Group group = groups.getOrDefault(state.getGroup().getId(), new Group(state.getGroup()));
            group.members.add(state);
            group.members.sort(Comparator.comparing(PlayerState::getName));
            groups.put(state.getGroup().getId(), group);
        }
        return new ArrayList<>(groups.values());
    }

    @Override
    public void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        List<Group> entries = getGroups();
        for (int i = getOffset(); i < entries.size() && i < getOffset() + columnCount; i++) {
            int pos = i - getOffset();
            VoiceChatScreenBase.HoverArea hoverArea = hoverAreas[pos];
            int startY = guiTop + pos * columnHeight;
            Group group = entries.get(i);
            boolean hasPassword = group.group.hasPassword();

            if (hasPassword) {
                matrixStack.pushPose();
                mc.getTextureManager().bind(LOCK);
                Screen.blit(matrixStack, guiLeft + 3, startY + 3, 0, 0, 16, 16, 16, 16);
                matrixStack.popPose();
            }

            StringTextComponent groupName = new StringTextComponent(group.group.getName());
            mc.font.draw(matrixStack, groupName, guiLeft + 3 + (hasPassword ? 16 + 3 : 0), startY + 7, 0);

            int textWidth = mc.font.width(groupName) + (hasPassword ? 16 + 3 : 0);

            int headsPerRow = (xSize - (3 + textWidth + 3 + 3)) / (8 + 1);

            for (int j = 0; j < group.members.size(); j++) {
                PlayerState state = group.members.get(j);
                int headXIndex = j % headsPerRow;
                int headYIndex = j / headsPerRow;

                if (headYIndex > 1) {
                    break;
                }

                int headPosX = guiLeft + xSize - 8 - 2 - headXIndex * 9;
                int headPosY = startY + 2 + 10 - 10 * headYIndex;

                matrixStack.pushPose();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                mc.getTextureManager().bind(SkinUtils.getSkin(state.getUuid()));
                matrixStack.translate(headPosX, headPosY, 0);
                Screen.blit(matrixStack, 0, 0, 8, 8, 8, 8, 64, 64);
                Screen.blit(matrixStack, 0, 0, 40, 8, 8, 8, 64, 64);
                matrixStack.popPose();
            }

            if (hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                List<IReorderingProcessor> tooltip = new ArrayList<>();
                tooltip.add(new TranslationTextComponent("message.voicechat.group_members").withStyle(TextFormatting.WHITE).getVisualOrderText());
                for (PlayerState state : group.members) {
                    tooltip.add(new StringTextComponent("- " + state.getName()).withStyle(TextFormatting.GRAY).getVisualOrderText());
                }
                screen.renderTooltip(matrixStack, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        List<Group> entries = getGroups();
        for (int i = getOffset(); i < entries.size() && i < getOffset() + columnCount; i++) {
            mc.getTextureManager().bind(TEXTURE);
            int pos = i - getOffset();
            VoiceChatScreenBase.HoverArea hoverArea = hoverAreas[pos];
            boolean hovered = hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY);
            int startY = guiTop + pos * columnHeight;
            Group group = entries.get(i);

            if (hovered) {
                Screen.blit(matrixStack, guiLeft, startY, 195, 39, 160, columnHeight, 512, 512);
            } else {
                Screen.blit(matrixStack, guiLeft, startY, 195, 17, 160, columnHeight, 512, 512);
            }
        }

        mc.getTextureManager().bind(TEXTURE);

        if (entries.size() > columnCount) {
            float h = ySize - 17;
            float perc = (float) getOffset() / (float) (entries.size() - columnCount);
            int posY = guiTop + (int) (h * perc);
            Screen.blit(matrixStack, guiLeft + xSize + 6, posY, 195, 0, 12, 17, 512, 512);
        } else {
            Screen.blit(matrixStack, guiLeft + xSize + 6, guiTop, 207, 0, 12, 17, 512, 512);
        }
    }

    public int getOffset() {
        List<Group> entries = getGroups();
        if (entries.size() <= columnCount) {
            offset = 0;
        } else if (offset > entries.size() - columnCount) {
            offset = entries.size() - columnCount;
        }
        return offset;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<Group> entries = getGroups();
        if (entries.size() > columnCount) {
            if (delta < 0D) {
                offset = Math.min(getOffset() + 1, entries.size() - columnCount);
            } else {
                offset = Math.max(getOffset() - 1, 0);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<Group> entries = getGroups();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= entries.size()) {
                break;
            }
            if (!hoverAreas[i].isHovered(guiLeft, guiTop, (int) mouseX, (int) mouseY)) {
                continue;
            }
            Group group = entries.get(getOffset() + i);
            mc.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            if (group.group.hasPassword()) {
                mc.setScreen(new EnterPasswordScreen(group.group));
            } else {
                NetManager.sendToServer(new JoinGroupPacket(group.group.getId(), null));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public static class Group {
        private final ClientGroup group;
        private final List<PlayerState> members;

        public Group(ClientGroup group) {
            this.group = group;
            this.members = new ArrayList<>();
        }
    }

}
