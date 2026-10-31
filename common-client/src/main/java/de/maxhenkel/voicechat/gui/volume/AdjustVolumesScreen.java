package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ButtonBase;
import de.maxhenkel.voicechat.gui.widgets.IngameListScreenBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdjustVolumesScreen extends IngameListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_volumes.png");
    protected static final ITextComponent TITLE = new TextComponentTranslation("gui.voicechat.adjust_volume.title");
    protected static final ITextComponent SEARCH_HINT = new TextComponentTranslation("message.voicechat.search_hint").setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true));
    protected static final ITextComponent EMPTY_SEARCH = new TextComponentTranslation("message.voicechat.search_empty").setStyle(new Style().setColor(TextFormatting.GRAY));

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
    protected GuiTextField searchBox;
    protected List<VolumeTab> tabs;
    protected final List<ButtonBase> tabButtons;
    protected ButtonBase scrollTabsLeft;
    protected ButtonBase scrollTabsRight;
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
    public void updateScreen() {
        super.updateScreen();
        searchBox.updateCursorCounter();
    }

    @Override
    public void initGui() {
        super.initGui();
        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = MathHelper.ceil((float) (CELL_HEIGHT + SEARCH_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        Keyboard.enableRepeatEvents(true);

        volumeList = new AdjustVolumeList(width, units * UNIT_SIZE - SEARCH_HEIGHT, guiTop + HEADER_SIZE + SEARCH_HEIGHT, CELL_HEIGHT, this, volumeList != null ? volumeList.getTab() : tabs.get(0));

        String string = searchBox != null ? searchBox.getText() : "";
        searchBox = new GuiTextField(0, fontRenderer, guiLeft + 28, guiTop + HEADER_SIZE + 6, 196, SEARCH_HEIGHT);
        searchBox.setMaxStringLength(16);
        searchBox.setEnableBackgroundDrawing(false);
        searchBox.setVisible(true);
        searchBox.setTextColor(0xFFFFFF);
        searchBox.setText(string);
        searchBox.setGuiResponder(new GuiPageButtonList.GuiResponder() {
            @Override
            public void setEntryValue(int id, boolean value) {
            }

            @Override
            public void setEntryValue(int id, float value) {
            }

            @Override
            public void setEntryValue(int id, String value) {
                checkSearchStringUpdate(value);
            }
        });
        setList(volumeList);

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

        scrollTabsLeft = new ButtonBase(0, guiLeft, tabY, SCROLL_BUTTON_WIDTH, TAB_HEIGHT, "<") {
            @Override
            public void onPress() {
                scrollTabs(-1);
            }
        };
        scrollTabsLeft.visible = scrollable;
        addButton(scrollTabsLeft);
        scrollTabsRight = new ButtonBase(1, guiLeft + xSize - SCROLL_BUTTON_WIDTH, tabY, SCROLL_BUTTON_WIDTH, TAB_HEIGHT, ">") {
            @Override
            public void onPress() {
                scrollTabs(1);
            }
        };
        scrollTabsRight.visible = scrollable;
        addButton(scrollTabsRight);

        int visibleTabs = Math.min(tabs.size(), (tabsWidth + TAB_SPACING) / (MIN_TAB_WIDTH + TAB_SPACING));
        int tabWidth = (tabsWidth - TAB_SPACING * (visibleTabs - 1)) / visibleTabs;
        tabButtons.clear();
        for (int i = 0; i < visibleTabs; i++) {
            int index = i;
            ButtonBase tabButton = new ButtonBase(2 + i, tabsX + i * (tabWidth + TAB_SPACING), tabY, tabWidth, TAB_HEIGHT, "") {
                @Override
                public void onPress() {
                    selectTab(tabs.get(tabScroll + index));
                }
            };
            tabButtons.add(tabButton);
            addButton(tabButton);
        }
        scrollTabs(0);
    }

    public static void updateCategories() {
        if (Minecraft.getMinecraft().currentScreen instanceof AdjustVolumesScreen) {
            AdjustVolumesScreen volumesScreen = (AdjustVolumesScreen) Minecraft.getMinecraft().currentScreen;
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
        removeButton(scrollTabsLeft);
        removeButton(scrollTabsRight);
        tabButtons.forEach(this::removeButton);
        initTabButtons();
    }

    private void removeButton(ButtonBase button) {
        buttonList.remove(button);
    }

    private void scrollTabs(int amount) {
        tabScroll = MathHelper.clamp(tabScroll + amount, 0, tabs.size() - tabButtons.size());
        updateTabButtons();
    }

    private void selectTab(VolumeTab tab) {
        volumeList.setTab(tab);
        updateTabButtons();
    }

    private void updateTabButtons() {
        for (int i = 0; i < tabButtons.size(); i++) {
            VolumeTab tab = tabs.get(tabScroll + i);
            ButtonBase tabButton = tabButtons.get(i);
            tabButton.displayString = tab.getTitle().getUnformattedComponentText();
            tabButton.enabled = !tab.getId().equals(volumeList.getTab().getId());
        }
        scrollTabsLeft.enabled = tabScroll > 0;
        scrollTabsRight.enabled = tabScroll < tabs.size() - tabButtons.size();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void renderBackground(int mouseX, int mouseY, float delta) {
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, HEADER_SIZE);
        for (int i = 0; i < units; i++) {
            drawTexturedModalRect(guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * i, 0, HEADER_SIZE, xSize, UNIT_SIZE);
        }
        drawTexturedModalRect(guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * units, 0, HEADER_SIZE + UNIT_SIZE, xSize, FOOTER_SIZE);
        drawTexturedModalRect(guiLeft + 10, guiTop + HEADER_SIZE + 6 - 2, xSize, 0, 12, 12);
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, float delta) {
        fontRenderer.drawString(TITLE.getFormattedText(), width / 2 - fontRenderer.getStringWidth(TITLE.getUnformattedComponentText()) / 2, guiTop + 5, VoiceChatScreenBase.FONT_COLOR);

        if (volumeList == null) {
            return;
        }

        if (!volumeList.isEmpty()) {
            volumeList.drawScreen(mouseX, mouseY, delta);
        } else if (!searchBox.getText().isEmpty()) {
            drawCenteredString(fontRenderer, EMPTY_SEARCH.getFormattedText(), width / 2, guiTop + HEADER_SIZE + (units * UNIT_SIZE) / 2 - fontRenderer.FONT_HEIGHT / 2, -1);
        }
        if (!searchBox.isFocused() && searchBox.getText().isEmpty()) {
            drawString(fontRenderer, SEARCH_HINT.getFormattedText(), searchBox.x, searchBox.y, -1);
        } else {
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            searchBox.drawTextBox();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (volumeList == null) {
            return;
        }
        searchBox.mouseClicked(mouseX, mouseY, mouseButton);
        volumeList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (searchBox == null) {
            return;
        }
        searchBox.textboxKeyTyped(typedChar, keyCode);
    }

    private void checkSearchStringUpdate(String string) {
        if (!(string = string.toLowerCase(Locale.ROOT)).equals(lastSearch)) {
            volumeList.setFilter(string);
            lastSearch = string;
        }
    }

}