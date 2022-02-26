package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.GameProfileUtils;
import de.maxhenkel.voicechat.gui.widgets.ListScreenEntryBase;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import javax.annotation.Nullable;

public class PlayerVolumeEntry extends ListScreenEntryBase<PlayerVolumeEntry> {

    protected static final TranslatableComponent SYSTEM_VOLUME = new TranslatableComponent("message.voicechat.system_volume");
    protected static final ResourceLocation SYSTEM_VOLUME_ICON = new ResourceLocation(Voicechat.MODID, "textures/icons/system_volume.png");

    protected static final int SKIN_SIZE = 24;
    protected static final int PADDING = 4;
    protected static final int BG_FILL = FastColor.ARGB32.color(255, 74, 74, 74);
    protected static final int PLAYER_NAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);

    protected final Minecraft minecraft;
    @Nullable
    protected final PlayerState state;
    protected final AdjustVolumeSlider volumeSlider;

    public PlayerVolumeEntry(@Nullable PlayerState state) {
        this.minecraft = Minecraft.getInstance();
        this.state = state;
        this.volumeSlider = new AdjustVolumeSlider(0, 0, 100, 20, state != null ? state.getUuid() : Util.NIL_UUID);
        this.children.add(volumeSlider);
    }

    @Override
    public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        int skinX = left + PADDING;
        int skinY = top + (height - SKIN_SIZE) / 2;
        int textX = skinX + SKIN_SIZE + PADDING;
        int textY = top + (height - minecraft.font.lineHeight) / 2;

        GuiComponent.fill(poseStack, left, top, left + width, top + height, BG_FILL);

        if (state != null) {
            RenderSystem.setShaderTexture(0, GameProfileUtils.getSkin(state.getUuid()));
            GuiComponent.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 8, 8, 8, 8, 64, 64);
            RenderSystem.enableBlend();
            GuiComponent.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 40, 8, 8, 8, 64, 64);
            RenderSystem.disableBlend();
            minecraft.font.draw(poseStack, state.getName(), (float) textX, (float) textY, PLAYER_NAME_COLOR);
        } else {
            RenderSystem.setShaderTexture(0, SYSTEM_VOLUME_ICON);
            GuiComponent.blit(poseStack, skinX, skinY, SKIN_SIZE, SKIN_SIZE, 16, 16, 16, 16, 16, 16);
            minecraft.font.draw(poseStack, SYSTEM_VOLUME, (float) textX, (float) textY, PLAYER_NAME_COLOR);
        }

        volumeSlider.x = left + (width - volumeSlider.getWidth() - PADDING);
        volumeSlider.y = top + (height - volumeSlider.getHeight()) / 2;
        volumeSlider.render(poseStack, mouseX, mouseY, delta);
    }

    @Nullable
    public PlayerState getState() {
        return state;
    }
}
