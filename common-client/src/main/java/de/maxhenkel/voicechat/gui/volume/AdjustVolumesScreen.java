package de.maxhenkel.voicechat.gui.volume;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdjustVolumesScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_volumes.png");
    protected static final Component TITLE = new TranslatableComponent("gui.voicechat.adjust_volume.title");
    protected static final Component SEARCH_HINT = new TranslatableComponent("message.voicechat.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    protected static final Component EMPTY_SEARCH = new TranslatableComponent("message.voicechat.search_empty").withStyle(ChatFormatting.GRAY);

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 8;
    protected static final int SEARCH_HEIGHT = 16;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;
    protected static final int TAB_HEIGHT = 20;
    protected static final int TAB_SPACING = 2;
    protected static final int MIN_TAB_WIDTH = 50;
    protected static final int SCROLL_BUTTON_WIDTH = 20;

    protected AdjustVolumeList volumeList;
    protected EditBox searchBox;
    protected List<VolumeTab> tabs;
    protected final List<Button> tabButtons;
    protected Button scrollTabsLeft;
    protected Button scrollTabsRight;
    protected int tabScroll;
    protected String lastSearch;
    protected int units;

    public AdjustVolumesScreen() {
        super(TITLE, 236, 0);
        this.lastSearch = "";
        this.tabs = VolumeTab.createTabs();
        this.tabButtons = new ArrayList<>();
    }

    @Override
    public void tick() {
        super.tick();
        searchBox.tick();
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = Mth.ceil((float) (CELL_HEIGHT + SEARCH_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        if (volumeList != null) {
            volumeList.updateSize(width, units * UNIT_SIZE - SEARCH_HEIGHT, guiTop + HEADER_SIZE + SEARCH_HEIGHT);
        } else {
            volumeList = new AdjustVolumeList(width, units * UNIT_SIZE - SEARCH_HEIGHT, guiTop + HEADER_SIZE + SEARCH_HEIGHT, CELL_HEIGHT, this, tabs.get(0));
        }
        String string = searchBox != null ? searchBox.getValue() : "";
        searchBox = new EditBox(font, guiLeft + 28, guiTop + HEADER_SIZE + 6, 196, SEARCH_HEIGHT, SEARCH_HINT);
        searchBox.setMaxLength(16);
        searchBox.setBordered(false);
        searchBox.setVisible(true);
        searchBox.setTextColor(0xFFFFFF);
        searchBox.setValue(string);
        searchBox.setResponder(this::checkSearchStringUpdate);
        addWidget(searchBox);
        addWidget(volumeList);

        initTabButtons();
    }

    private void initTabButtons() {
        int tabY = guiTop - TAB_HEIGHT - TAB_SPACING;
        int tabsX = guiLeft;
        int tabsWidth = xSize;
        boolean scrollable = tabs.size() * (MIN_TAB_WIDTH + TAB_SPACING) - TAB_SPACING > xSize;
        if (scrollable) {
            tabsX += SCROLL_BUTTON_WIDTH + TAB_SPACING;
            tabsWidth -= (SCROLL_BUTTON_WIDTH + TAB_SPACING) * 2;
        }

        scrollTabsLeft = new Button(guiLeft, tabY, SCROLL_BUTTON_WIDTH, TAB_HEIGHT, new TextComponent("<"), button -> {
            scrollTabs(-1);
        });
        scrollTabsLeft.visible = scrollable;
        addRenderableWidget(scrollTabsLeft);
        scrollTabsRight = new Button(guiLeft + xSize - SCROLL_BUTTON_WIDTH, tabY, SCROLL_BUTTON_WIDTH, TAB_HEIGHT, new TextComponent(">"), button -> {
            scrollTabs(1);
        });
        scrollTabsRight.visible = scrollable;
        addRenderableWidget(scrollTabsRight);

        int visibleTabs = Math.min(tabs.size(), (tabsWidth + TAB_SPACING) / (MIN_TAB_WIDTH + TAB_SPACING));
        int tabWidth = (tabsWidth - TAB_SPACING * (visibleTabs - 1)) / visibleTabs;
        tabButtons.clear();
        for (int i = 0; i < visibleTabs; i++) {
            int index = i;
            Button tabButton = new Button(tabsX + i * (tabWidth + TAB_SPACING), tabY, tabWidth, TAB_HEIGHT, new TextComponent(""), button -> {
                selectTab(tabs.get(tabScroll + index));
            });
            tabButtons.add(tabButton);
            addRenderableWidget(tabButton);
        }
        scrollTabs(0);
    }

    public static void updateCategories() {
        if (Minecraft.getInstance().screen instanceof AdjustVolumesScreen volumesScreen) {
            volumesScreen.updateTabs();
        }
    }

    private void updateTabs() {
        tabs = VolumeTab.createTabs();
        if (tabs.stream().anyMatch(tab -> tab.getId().equals(volumeList.getTab().getId()))) {
            volumeList.updateEntryList();
        } else {
            volumeList.setTab(tabs.get(0));
        }
        removeWidget(scrollTabsLeft);
        removeWidget(scrollTabsRight);
        tabButtons.forEach(this::removeWidget);
        initTabButtons();
    }

    private void scrollTabs(int amount) {
        tabScroll = Mth.clamp(tabScroll + amount, 0, tabs.size() - tabButtons.size());
        updateTabButtons();
    }

    private void selectTab(VolumeTab tab) {
        volumeList.setTab(tab);
        updateTabButtons();
    }

    private void updateTabButtons() {
        for (int i = 0; i < tabButtons.size(); i++) {
            VolumeTab tab = tabs.get(tabScroll + i);
            Button tabButton = tabButtons.get(i);
            tabButton.setMessage(tab.getTitle());
            tabButton.active = !tab.getId().equals(volumeList.getTab().getId());
        }
        scrollTabsLeft.active = tabScroll > 0;
        scrollTabsRight.active = tabScroll < tabs.size() - tabButtons.size();
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, HEADER_SIZE);
        for (int i = 0; i < units; i++) {
            blit(poseStack, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * i, 0, HEADER_SIZE, xSize, UNIT_SIZE);
        }
        blit(poseStack, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * units, 0, HEADER_SIZE + UNIT_SIZE, xSize, FOOTER_SIZE);
        blit(poseStack, guiLeft + 10, guiTop + HEADER_SIZE + 6 - 2, xSize, 0, 12, 12);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        font.draw(poseStack, TITLE, width / 2 - font.width(TITLE) / 2, guiTop + 5, VoiceChatScreenBase.FONT_COLOR);
        if (!volumeList.isEmpty()) {
            volumeList.render(poseStack, mouseX, mouseY, delta);
        } else if (!searchBox.getValue().isEmpty()) {
            drawCenteredString(poseStack, font, EMPTY_SEARCH, width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 - font.lineHeight / 2, -1);
        }
        if (!searchBox.isFocused() && searchBox.getValue().isEmpty()) {
            drawString(poseStack, font, SEARCH_HINT, searchBox.x, searchBox.y, -1);
        } else {
            searchBox.render(poseStack, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchBox.isFocused()) {
            searchBox.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button) || volumeList.mouseClicked(mouseX, mouseY, button);
    }

    private void checkSearchStringUpdate(String string) {
        if (!(string = string.toLowerCase(Locale.ROOT)).equals(lastSearch)) {
            volumeList.setFilter(string);
            lastSearch = string;
        }
    }

}