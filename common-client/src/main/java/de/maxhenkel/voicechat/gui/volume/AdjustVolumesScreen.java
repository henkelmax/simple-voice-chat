package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdjustVolumesScreen extends VoiceChatScreenBase {

    protected static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Voicechat.MODID, "textures/gui/gui_volumes.png");
    protected static final Component TITLE = Component.translatable("gui.voicechat.adjust_volume.title");
    protected static final Component SEARCH_HINT = Component.translatable("message.voicechat.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    protected static final Component EMPTY_SEARCH = Component.translatable("message.voicechat.search_empty").withStyle(ChatFormatting.GRAY);
    protected static final Component NO_PLAYERS = Component.translatable("message.voicechat.no_players").withStyle(ChatFormatting.GRAY);

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
    protected void init() {
        super.init();
        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = Mth.ceil((float) (CELL_HEIGHT + SEARCH_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        if (volumeList != null) {
            volumeList.updateSize(width, units * UNIT_SIZE - SEARCH_HEIGHT, 0, guiTop + HEADER_SIZE + SEARCH_HEIGHT);
        } else {
            volumeList = new AdjustVolumeList(width, units * UNIT_SIZE - SEARCH_HEIGHT, guiTop + HEADER_SIZE + SEARCH_HEIGHT, CELL_HEIGHT, this, tabs.get(0));
        }
        String string = searchBox != null ? searchBox.getValue() : "";
        searchBox = new EditBox(font, guiLeft + 28, guiTop + HEADER_SIZE + 6, 196, SEARCH_HEIGHT, SEARCH_HINT);
        searchBox.setMaxLength(16);
        searchBox.setBordered(false);
        searchBox.setVisible(true);
        searchBox.setTextColor(-1);
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

        scrollTabsLeft = Button.builder(Component.literal("<"), button -> {
            scrollTabs(-1);
        }).bounds(guiLeft, tabY, SCROLL_BUTTON_WIDTH, TAB_HEIGHT).build();
        scrollTabsLeft.visible = scrollable;
        addRenderableWidget(scrollTabsLeft);
        scrollTabsRight = Button.builder(Component.literal(">"), button -> {
            scrollTabs(1);
        }).bounds(guiLeft + xSize - SCROLL_BUTTON_WIDTH, tabY, SCROLL_BUTTON_WIDTH, TAB_HEIGHT).build();
        scrollTabsRight.visible = scrollable;
        addRenderableWidget(scrollTabsRight);

        int visibleTabs = Math.min(tabs.size(), (tabsWidth + TAB_SPACING) / (MIN_TAB_WIDTH + TAB_SPACING));
        int tabWidth = (tabsWidth - TAB_SPACING * (visibleTabs - 1)) / visibleTabs;
        tabButtons.clear();
        for (int i = 0; i < visibleTabs; i++) {
            int index = i;
            Button tabButton = Button.builder(Component.empty(), button -> {
                selectTab(tabs.get(tabScroll + index));
            }).bounds(tabsX + i * (tabWidth + TAB_SPACING), tabY, tabWidth, TAB_HEIGHT).build();
            tabButtons.add(tabButton);
            addRenderableWidget(tabButton);
        }
        scrollTabs(0);
    }

    public static void updateCategories() {
        if (Minecraft.getInstance().gui.screen() instanceof AdjustVolumesScreen volumesScreen) {
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
    public void extractBackgroundRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop, 0, 0, xSize, HEADER_SIZE, 256, 256);
        for (int i = 0; i < units; i++) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * i, 0, HEADER_SIZE, xSize, UNIT_SIZE, 256, 256);
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * units, 0, HEADER_SIZE + UNIT_SIZE, xSize, FOOTER_SIZE, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, guiLeft + 10, guiTop + HEADER_SIZE + 6 - 2, xSize, 0, 12, 12, 256, 256);
    }

    @Override
    public void extractForegroundRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.text(font, TITLE, width / 2 - font.width(TITLE) / 2, guiTop + 5, VoiceChatScreenBase.FONT_COLOR, false);
        if (!volumeList.isEmpty()) {
            volumeList.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        } else if (!searchBox.getValue().isEmpty()) {
            guiGraphics.centeredText(font, EMPTY_SEARCH, width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 - font.lineHeight / 2, -1);
        } else if (volumeList.getTab().getId().equals(VolumeTab.PLAYERS)) {
            guiGraphics.centeredText(font, NO_PLAYERS, width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 - font.lineHeight / 2, -1);
        }
        if (!searchBox.isFocused() && searchBox.getValue().isEmpty()) {
            guiGraphics.text(font, SEARCH_HINT, searchBox.getX(), searchBox.getY(), -1, false);
        } else {
            searchBox.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (searchBox.isFocused()) {
            searchBox.mouseClicked(mouseButtonEvent, bl);
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    private void checkSearchStringUpdate(String string) {
        if (!(string = string.toLowerCase(Locale.ROOT)).equals(lastSearch)) {
            volumeList.setFilter(string);
            lastSearch = string;
        }
    }

}