package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Locale;

public class PlayerVolumesScreen extends Screen {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_player_volumes.png");
    private static final Component ADJUST_VOLUMES = new TranslatableComponent("gui.voicechat.adjust_volume.title");
    private static final Component SEARCH_HINT = new TranslatableComponent("message.voicechat.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    private static final Component EMPTY_SEARCH = new TranslatableComponent("gui.socialInteractions.search_empty").withStyle(ChatFormatting.GRAY);

    private static final int BG_BORDER_SIZE = 8;
    private static final int BG_UNITS = 16;
    private static final int BG_WIDTH = 236;
    private static final int SEARCH_HEIGHT = 16;
    private static final int MARGIN_Y = 64;
    public static final int LIST_START = 88 + 8;
    public static final int SEARCH_START = 78 + 8;
    private static final int IMAGE_WIDTH = 238;
    private static final int ITEM_HEIGHT = 36;

    private AdjustVolumeList volumeList;
    private EditBox searchBox;
    private String lastSearch;
    private boolean initialized;

    public PlayerVolumesScreen() {
        super(ADJUST_VOLUMES);
        this.lastSearch = "";
    }

    private int windowHeight() {
        return Math.max(52, height - 128 - SEARCH_HEIGHT);
    }

    private int backgroundUnits() {
        return windowHeight() / BG_UNITS;
    }

    private int listEnd() {
        return 80 + 8 + backgroundUnits() * BG_UNITS - BG_BORDER_SIZE;
    }

    private int marginX() {
        return (width - IMAGE_WIDTH) / 2;
    }

    @Override
    public void tick() {
        super.tick();
        searchBox.tick();
    }

    @Override
    protected void init() {
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        if (initialized) {
            volumeList.updateSize(width, height, LIST_START, listEnd());
        } else {
            volumeList = new AdjustVolumeList(width, height, LIST_START, listEnd(), ITEM_HEIGHT);
        }
        String string = searchBox != null ? searchBox.getValue() : "";
        searchBox = new EditBox(font, marginX() + 28, SEARCH_START, 196, SEARCH_HEIGHT, SEARCH_HINT);
        searchBox.setMaxLength(16);
        searchBox.setBordered(false);
        searchBox.setVisible(true);
        searchBox.setTextColor(0xFFFFFF);
        searchBox.setValue(string);
        searchBox.setResponder(this::checkSearchStringUpdate);
        addWidget(searchBox);
        addWidget(volumeList);
        initialized = true;
        loadEntries();
    }

    private void loadEntries() {
        Collection<PlayerState> collection = ClientManager.getPlayerStateManager().getPlayerStates(false);
        volumeList.updatePlayerList(collection, volumeList.getScrollAmount());
    }

    @Override
    public void removed() {
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        int x = marginX() + 3;
        super.renderBackground(poseStack);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, x, MARGIN_Y, 1, 1, BG_WIDTH, 8 + 8);
        int units = backgroundUnits();
        for (int unit = 0; unit < units; unit++) {
            blit(poseStack, x, 72 + 8 + BG_UNITS * unit, 1, 10 + 8, BG_WIDTH, BG_UNITS);
        }
        blit(poseStack, x, 72 + 8 + BG_UNITS * units, 1, 27 + 8, BG_WIDTH, 8);
        blit(poseStack, x + 10, SEARCH_START - 2, 243, 1, 12, 12);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);
        font.draw(poseStack, ADJUST_VOLUMES, width / 2 - font.width(ADJUST_VOLUMES) / 2, MARGIN_Y + 5, VoiceChatScreenBase.FONT_COLOR);
        if (!volumeList.isEmpty()) {
            volumeList.render(poseStack, mouseX, mouseY, partialTicks);
        } else if (!searchBox.getValue().isEmpty()) {
            drawCenteredString(poseStack, minecraft.font, EMPTY_SEARCH, width / 2, (SEARCH_START + listEnd()) / 2, -1);
        }
        if (!searchBox.isFocused() && searchBox.getValue().isEmpty()) {
            drawString(poseStack, minecraft.font, SEARCH_HINT, searchBox.x, searchBox.y, -1);
        } else {
            searchBox.render(poseStack, mouseX, mouseY, partialTicks);
        }
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchBox.isFocused()) {
            searchBox.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button) || volumeList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!searchBox.isFocused()) {
            minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void checkSearchStringUpdate(String string) {
        if (!(string = string.toLowerCase(Locale.ROOT)).equals(lastSearch)) {
            volumeList.setFilter(string);
            lastSearch = string;
        }
    }

}