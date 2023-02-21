package de.maxhenkel.voicechat.gui.volume;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.VoiceChatScreenBase;
import de.maxhenkel.voicechat.gui.widgets.ListScreenBase;
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
import java.util.Locale;

public class AdjustVolumesScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_volumes.png");
    protected static final ITextComponent TITLE = new TextComponentTranslation("gui.voicechat.adjust_volume.title");
    protected static final ITextComponent SEARCH_HINT = new TextComponentTranslation("message.voicechat.search_hint").setStyle(new Style().setColor(TextFormatting.GRAY).setItalic(true));
    protected static final ITextComponent EMPTY_SEARCH = new TextComponentTranslation("message.voicechat.search_empty").setStyle(new Style().setColor(TextFormatting.GRAY));

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 8;
    protected static final int SEARCH_HEIGHT = 16;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;

    protected AdjustVolumeList volumeList;
    protected GuiTextField searchBox;
    protected String lastSearch;
    protected int units;

    public AdjustVolumesScreen() {
        super(TITLE, 236, 0);
        this.lastSearch = "";
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
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2 - SEARCH_HEIGHT) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        Keyboard.enableRepeatEvents(true);

        volumeList = new AdjustVolumeList(width, height, guiTop + HEADER_SIZE + SEARCH_HEIGHT, guiTop + HEADER_SIZE + units * UNIT_SIZE, CELL_HEIGHT, this);
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