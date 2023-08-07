package de.maxhenkel.voicechat.gui.group;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class GroupEntry extends ListScreenEntryBase<GroupEntry> {

    protected static final ResourceLocation TALK_OUTLINE = new ResourceLocation(Voicechat.MODID, "textures/icons/talk_outline.png");
    protected static final ResourceLocation SPEAKER_OFF = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker_small_off.png");

    protected static final int PADDING = 4;
    protected static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    protected static final int PLAYER_NAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);

    protected final ListScreenBase parent;
    protected final Minecraft minecraft;
    protected PlayerState state;
    protected final AdjustVolumeSlider volumeSlider;

    public GroupEntry(ListScreenBase parent, PlayerState state) {
        this.parent = parent;
        this.minecraft = Minecraft.getInstance();
        this.state = state;
        this.volumeSlider = new AdjustVolumeSlider(0, 0, 100, 20, new PlayerVolumeEntry.PlayerVolumeConfigEntry(state.getUuid()));
        this.children.add(volumeSlider);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        guiGraphics.fill(left, top, left + width, top + height, BG_FILL);

        guiGraphics.pose().pushPose();
        int outlineSize = height - PADDING * 2;

        guiGraphics.pose().translate(left + PADDING, top + PADDING, 0D);
        float scale = outlineSize / 10F;
        guiGraphics.pose().scale(scale, scale, scale);

        if (!state.isDisabled()) {
            ClientVoicechat client = ClientManager.getClient();
            if (client != null && client.getTalkCache().isTalking(state.getUuid())) {
                guiGraphics.blit(TALK_OUTLINE, 0, 0, 0, 0, 10, 10, 16, 16);
            }
        }

        PlayerSkin skin = GameProfileUtils.getSkin(state.getUuid());
        guiGraphics.blit(skin.texture(), 1, 1, 8, 8, 8, 8, 8, 8, 64, 64);
        RenderSystem.enableBlend();
        guiGraphics.blit(skin.texture(), 1, 1, 8, 8, 40, 8, 8, 8, 64, 64);
        RenderSystem.disableBlend();

        if (state.isDisabled()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(1D, 1D, 0D);
            guiGraphics.pose().scale(0.5F, 0.5F, 1F);
            guiGraphics.blit(SPEAKER_OFF, 0, 0, 0, 0, 16, 16, 16, 16);
            guiGraphics.pose().popPose();
        }
        guiGraphics.pose().popPose();

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
