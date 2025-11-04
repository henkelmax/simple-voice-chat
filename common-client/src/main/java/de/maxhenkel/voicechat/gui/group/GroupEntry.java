package de.maxhenkel.voicechat.gui.group;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeSlider;
import de.maxhenkel.voicechat.gui.volume.PlayerVolumeEntry;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.PlayerSkin;

public class GroupEntry extends ListScreenEntryBase<GroupEntry> {

    protected static final Identifier TALK_OUTLINE = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/talk_outline.png");
    protected static final Identifier SPEAKER_OFF = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/icons/speaker_small_off.png");

    protected static final int PADDING = 4;
    protected static final int BG_FILL = ARGB.color(255, 74, 74, 74);
    protected static final int PLAYER_NAME_COLOR = ARGB.color(255, 255, 255, 255);

    protected final ListScreenBase parent;
    protected final Minecraft minecraft;
    protected PlayerState state;
    protected final AdjustVolumeSlider volumeSlider;

    public GroupEntry(ListScreenBase parent, PlayerState state) {
        this.parent = parent;
        this.minecraft = Minecraft.getInstance();
        this.state = state;
        this.volumeSlider = new AdjustVolumeSlider(0, 0, 100, 20, new PlayerVolumeEntry.AdjustPlayerVolumeEntry(state.getUuid(), state.getName()));
        this.children.add(volumeSlider);
    }

    @Override
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float delta) {
        int left = getContentX();
        int top = getContentY();
        int width = getContentWidth();
        int height = getContentHeight();
        guiGraphics.fill(left, top, left + width, top + height, BG_FILL);

        guiGraphics.pose().pushMatrix();
        int outlineSize = height - PADDING * 2;

        guiGraphics.pose().translate(left + PADDING, top + PADDING);
        float scale = outlineSize / 10F;
        guiGraphics.pose().scale(scale, scale);

        if (!state.isDisabled()) {
            ClientVoicechat client = ClientManager.getClient();
            if (client != null && client.getTalkCache().isTalking(state.getUuid())) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TALK_OUTLINE, 0, 0, 0, 0, 10, 10, 16, 16);
            }
        }

        PlayerSkin skin = GameProfileUtils.getSkin(state.getUuid());
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), 1, 1, 8, 8, 8, 8, 64, 64);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, skin.body().texturePath(), 1, 1, 40, 8, 8, 8, 64, 64);

        if (state.isDisabled()) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(1F, 1F);
            guiGraphics.pose().scale(0.5F, 0.5F);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPEAKER_OFF, 0, 0, 0, 0, 16, 16, 16, 16);
            guiGraphics.pose().popMatrix();
        }
        guiGraphics.pose().popMatrix();

        Component name = Component.literal(state.getName());
        guiGraphics.drawString(minecraft.font, name, left + PADDING + outlineSize + PADDING, top + height / 2 - minecraft.font.lineHeight / 2, PLAYER_NAME_COLOR, false);

        if (hovered && !ClientManager.getPlayerStateManager().getOwnID().equals(state.getUuid())) {
            volumeSlider.setWidth(Math.min(width - (PADDING + outlineSize + PADDING + minecraft.font.width(name) + PADDING + PADDING), 100));
            volumeSlider.setPosition(left + (width - volumeSlider.getWidth() - PADDING), top + (height - volumeSlider.getHeight()) / 2);
            volumeSlider.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }
}
