package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.SkinUtils;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.AdjustVolumeScreen;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class GroupList extends WidgetBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_group.png");
    private static final ResourceLocation SPEAKER_OFF = new ResourceLocation(Voicechat.MODID, "textures/gui/speaker_off.png");
    private static final ResourceLocation SPEAKER = new ResourceLocation(Voicechat.MODID, "textures/gui/speaker.png");
    private static final ResourceLocation CHANGE_VOLUME = new ResourceLocation(Voicechat.MODID, "textures/gui/change_volume.png");
    private static final ResourceLocation DISCONNECT = new ResourceLocation(Voicechat.MODID, "textures/gui/disconnected.png");

    protected Supplier<List<PlayerState>> playerStates;
    protected int offset;
    private VoiceChatScreenBase.HoverArea[] hoverAreas;
    private int columnHeight;
    private int columnCount;
    private Client voiceChatClient;

    public GroupList(VoiceChatScreenBase screen, int posX, int posY, int xSize, int ySize, Supplier<List<PlayerState>> playerStates) {
        super(screen, posX, posY, xSize, ySize);
        this.playerStates = playerStates;
        columnHeight = 22;
        columnCount = 8;

        hoverAreas = new VoiceChatScreenBase.HoverArea[columnCount];
        for (int i = 0; i < hoverAreas.length; i++) {
            hoverAreas[i] = new VoiceChatScreenBase.HoverArea(0, i * columnHeight, xSize, columnHeight);
        }

        voiceChatClient = VoicechatClient.CLIENT.getClient();

        if (voiceChatClient == null) {
            mc.setScreen(null);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!voiceChatClient.isConnected()) {
            mc.setScreen(null);
        }
    }

    @Override
    public void drawGuiContainerForegroundLayer(PoseStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        List<PlayerState> entries = playerStates.get();
        for (int i = getOffset(); i < entries.size() && i < getOffset() + columnCount; i++) {
            int pos = i - getOffset();
            int startY = guiTop + pos * columnHeight;
            PlayerState state = entries.get(i);
            mc.font.draw(matrixStack, new TextComponent(state.getGameProfile().getName()), guiLeft + 3 + 16 + 1 + 8 + 3, startY + 7, 0);
        }
    }

    @Override
    public void drawGuiContainerBackgroundLayer(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        List<PlayerState> entries = playerStates.get();
        for (int i = getOffset(); i < entries.size() && i < getOffset() + columnCount; i++) {
            PlayerState state = entries.get(i);
            mc.getTextureManager().bind(TEXTURE);
            int pos = i - getOffset();
            VoiceChatScreenBase.HoverArea hoverArea = hoverAreas[pos];
            boolean hovered = hoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY) && !state.getGameProfile().getId().equals(mc.player.getUUID());
            int startY = guiTop + pos * columnHeight;

            if (hovered) {
                Screen.blit(matrixStack, guiLeft, startY, 195, 39, 160, columnHeight, 512, 512);
            } else {
                Screen.blit(matrixStack, guiLeft, startY, 195, 17, 160, columnHeight, 512, 512);
            }

            matrixStack.pushPose();
            mc.getTextureManager().bind(SkinUtils.getSkin(state.getGameProfile()));
            matrixStack.translate(guiLeft + 3, startY + 3, 0);
            matrixStack.scale(2F, 2F, 1F);
            Screen.blit(matrixStack, 0, 0, 8, 8, 8, 8, 64, 64);
            Screen.blit(matrixStack, 0, 0, 40, 8, 8, 8, 64, 64);
            matrixStack.popPose();

            if (state.isDisconnected()) {
                drawIcon(matrixStack, startY, DISCONNECT);
            } else if (state.isDisabled()) {
                drawIcon(matrixStack, startY, SPEAKER_OFF);
            } else if (voiceChatClient.getTalkCache().isTalking(state.getGameProfile().getId())) {
                drawIcon(matrixStack, startY, SPEAKER);
            }

            mc.getTextureManager().bind(CHANGE_VOLUME);

            if (hovered) {
                Screen.blit(matrixStack, guiLeft + xSize - 3 - 16, startY + 3, 0, 0, 16, 16, 16, 16);
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

    private void drawIcon(PoseStack matrixStack, int startY, ResourceLocation texture) {
        matrixStack.pushPose();
        mc.getTextureManager().bind(texture);
        matrixStack.translate(guiLeft + 3 + 16 + 1, startY + 3 + 8, 0);
        matrixStack.scale(0.5F, 0.5F, 1F);
        Screen.blit(matrixStack, 0, 0, 0, 0, 16, 16, 16, 16);
        matrixStack.popPose();
    }

    public int getOffset() {
        List<PlayerState> entries = playerStates.get();
        if (entries.size() <= columnCount) {
            offset = 0;
        } else if (offset > entries.size() - columnCount) {
            offset = entries.size() - columnCount;
        }
        return offset;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<PlayerState> entries = playerStates.get();
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
        List<PlayerState> entries = playerStates.get();
        for (int i = 0; i < hoverAreas.length; i++) {
            if (getOffset() + i >= entries.size()) {
                break;
            }
            if (!hoverAreas[i].isHovered(guiLeft, guiTop, (int) mouseX, (int) mouseY)) {
                continue;
            }
            PlayerState state = entries.get(getOffset() + i);
            if (!state.getGameProfile().getId().equals(mc.player.getUUID())) {
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                mc.setScreen(new AdjustVolumeScreen(screen, Collections.singletonList(state)));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
