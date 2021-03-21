package de.maxhenkel.voicechat.gui.widgets;

import de.maxhenkel.voicechat.gui.SkinUtils;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.GroupScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Supplier;

public class CreateGroupList extends WidgetBase {

    private static final Identifier TEXTURE = new Identifier(Voicechat.MODID, "textures/gui/gui_create_group.png");

    protected Supplier<List<PlayerState>> playerStates;
    protected int offset;
    private VoiceChatScreenBase.HoverArea[] hoverAreas;
    private int columnHeight;
    private int columnCount;

    public CreateGroupList(VoiceChatScreenBase screen, int posX, int posY, int xSize, int ySize, Supplier<List<PlayerState>> playerStates) {
        super(screen, posX, posY, xSize, ySize);
        this.playerStates = playerStates;
        columnHeight = 22;
        columnCount = 4;

        hoverAreas = new VoiceChatScreenBase.HoverArea[columnCount];
        for (int i = 0; i < hoverAreas.length; i++) {
            hoverAreas[i] = new VoiceChatScreenBase.HoverArea(0, i * columnHeight, xSize, columnHeight);
        }
    }

    public List<Group> getGroups() {
        Map<String, Group> groups = new HashMap<>();
        List<PlayerState> playerStates = this.playerStates.get();

        for (PlayerState state : playerStates) {
            if (!state.hasGroup()) {
                continue;
            }

            Group group = groups.getOrDefault(state.getGroup(), new Group(state.getGroup()));
            group.members.add(state);
            group.members.sort(Comparator.comparing(o -> o.getGameProfile().getName()));
            groups.put(state.getGroup(), group);
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
            LiteralText groupName = new LiteralText(group.name);
            mc.textRenderer.draw(matrixStack, groupName, guiLeft + 3, startY + 7, 0);

            int textWidth = mc.textRenderer.getWidth(groupName);

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

                matrixStack.push();
                mc.getTextureManager().bindTexture(SkinUtils.getSkin(state.getGameProfile()));
                matrixStack.translate(headPosX, headPosY, 0);
                Screen.drawTexture(matrixStack, 0, 0, 8, 8, 8, 8, 64, 64);
                Screen.drawTexture(matrixStack, 0, 0, 40, 8, 8, 8, 64, 64);
                matrixStack.pop();
            }

            if (hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                List<OrderedText> tooltip = new ArrayList<>();
                tooltip.add(new TranslatableText("message.voicechat.group_members").formatted(Formatting.WHITE).asOrderedText());
                for (PlayerState state : group.members) {
                    tooltip.add(new LiteralText("- " + state.getGameProfile().getName()).formatted(Formatting.GRAY).asOrderedText());
                }
                screen.renderOrderedTooltip(matrixStack, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        List<Group> entries = getGroups();
        for (int i = getOffset(); i < entries.size() && i < getOffset() + columnCount; i++) {
            mc.getTextureManager().bindTexture(TEXTURE);
            int pos = i - getOffset();
            VoiceChatScreenBase.HoverArea hoverArea = hoverAreas[pos];
            boolean hovered = hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY);
            int startY = guiTop + pos * columnHeight;
            Group group = entries.get(i);

            if (hovered) {
                Screen.drawTexture(matrixStack, guiLeft, startY, 195, 39, 160, columnHeight, 512, 512);
            } else {
                Screen.drawTexture(matrixStack, guiLeft, startY, 195, 17, 160, columnHeight, 512, 512);
            }
        }

        mc.getTextureManager().bindTexture(TEXTURE);

        if (entries.size() > columnCount) {
            float h = ySize - 17;
            float perc = (float) getOffset() / (float) (entries.size() - columnCount);
            int posY = guiTop + (int) (h * perc);
            Screen.drawTexture(matrixStack, guiLeft + xSize + 6, posY, 195, 0, 12, 17, 512, 512);
        } else {
            Screen.drawTexture(matrixStack, guiLeft + xSize + 6, guiTop, 207, 0, 12, 17, 512, 512);
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
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1F));
            VoicechatClient.CLIENT.getPlayerStateManager().setGroup(group.name);
            mc.openScreen(new GroupScreen());
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public static class Group {
        private String name;
        private List<PlayerState> members;

        public Group(String name) {
            this.name = name;
            this.members = new ArrayList<>();
        }
    }

}
