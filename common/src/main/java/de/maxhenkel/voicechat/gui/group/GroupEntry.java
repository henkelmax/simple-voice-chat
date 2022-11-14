package de.maxhenkel.voicechat.gui.group;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeSlider;
import de.maxhenkel.voicechat.gui.volume.PlayerVolumeEntry;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GroupEntry extends ListScreenEntryBase {

    protected static final ResourceLocation TALK_OUTLINE = new ResourceLocation(Voicechat.MODID, "textures/icons/talk_outline.png");
    protected static final ResourceLocation SPEAKER_OFF = new ResourceLocation(Voicechat.MODID, "textures/icons/speaker_small_off.png");

    protected static final int PADDING = 4;
    protected static final int BG_FILL = VoiceChatScreenBase.color(255, 74, 74, 74);
    protected static final int PLAYER_NAME_COLOR = VoiceChatScreenBase.color(255, 255, 255, 255);

    protected final ListScreenBase parent;
    protected final Minecraft minecraft;
    protected PlayerState state;
    protected final AdjustVolumeSlider volumeSlider;

    public GroupEntry(ListScreenBase parent, PlayerState state) {
        this.parent = parent;
        this.minecraft = Minecraft.getMinecraft();
        this.state = state;
        this.volumeSlider = new AdjustVolumeSlider(0, 0, 0, 100, 20, new PlayerVolumeEntry.PlayerVolumeConfigEntry(state.getUuid()));
        this.children.add(volumeSlider);
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partialTicks);
        GuiScreen.drawRect(x, y, x + listWidth, y + slotHeight, BG_FILL);

        GlStateManager.pushMatrix();
        int outlineSize = slotHeight - PADDING * 2;

        GlStateManager.translate(x + PADDING, y + PADDING, 0D);
        float scale = outlineSize / 10F;
        GlStateManager.scale(scale, scale, scale);

        GlStateManager.color(1F, 1F, 1F, 1F);

        if (!state.isDisabled()) {
            ClientVoicechat client = ClientManager.getClient();
            if (client != null && client.getTalkCache().isTalking(state.getUuid())) {
                minecraft.getTextureManager().bindTexture(TALK_OUTLINE);
                GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 10, 10, 16, 16);
            }
        }

        minecraft.getTextureManager().bindTexture(GameProfileUtils.getSkin(state.getUuid()));

        Gui.drawScaledCustomSizeModalRect(1, 1, 8F, 8F, 8, 8, 8, 8, 64F, 64F);
        GlStateManager.enableBlend();
        Gui.drawScaledCustomSizeModalRect(1, 1, 40F, 8F, 8, 8, 8, 8, 64F, 64F);
        GlStateManager.disableBlend();

        if (state.isDisabled()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(1D, 1D, 0D);
            GlStateManager.scale(0.5F, 0.5F, 1F);
            minecraft.getTextureManager().bindTexture(SPEAKER_OFF);
            GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 16, 16, 16, 16);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();

        minecraft.fontRenderer.drawString(state.getName(), x + PADDING + outlineSize + PADDING, y + slotHeight / 2 - minecraft.fontRenderer.FONT_HEIGHT / 2, PLAYER_NAME_COLOR);

        if (isSelected && !ClientManager.getPlayerStateManager().getOwnID().equals(state.getUuid())) {
            volumeSlider.setWidth(Math.min(listWidth - (PADDING + outlineSize + PADDING + minecraft.fontRenderer.getStringWidth(state.getName()) + PADDING + PADDING), 100));
            volumeSlider.x = x + (listWidth - volumeSlider.width - PADDING);
            volumeSlider.y = y + (slotHeight - volumeSlider.height) / 2;
            volumeSlider.drawButton(minecraft, mouseX, mouseY, partialTicks);
        }
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }
}
