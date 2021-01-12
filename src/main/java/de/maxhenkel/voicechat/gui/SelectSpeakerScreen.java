package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.client.DataLines;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class SelectSpeakerScreen extends ListScreen<String> {

    protected int selected;

    public SelectSpeakerScreen() {
        super(DataLines.getSpeakerNames(), new TranslationTextComponent("gui.select_speaker.title"));
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i);
            if (element.equals(Main.CLIENT_CONFIG.speaker.get())) {
                index = i;
                selected = i;
                break;
            }
        }
    }

    @Override
    public void updateCurrentElement() {
        super.updateCurrentElement();
        String currentElement = getCurrentElement();
        if (currentElement == null) {
            return;
        }
        int bw = 60;
        Button b = addButton(new Button(width / 2 - bw / 2, guiTop + 35, bw, 20, new TranslationTextComponent("message.select").getString(), button -> {
            Main.CLIENT_CONFIG.speaker.set(currentElement);
            button.active = false;
            Client client = Main.CLIENT_VOICE_EVENTS.getClient();
            if (client != null) {
                client.reloadDataLines();
            }
        }));

        b.active = !currentElement.equals(Main.CLIENT_CONFIG.speaker.get());
    }

    @Override
    protected void renderText(@Nullable String element, int mouseX, int mouseY, float partialTicks) {
        ITextComponent title = getTitle();
        int titleWidth = font.getStringWidth(title.getString());
        font.drawString(title.getString(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);

        ITextComponent name = getCurrentElement() == null ? new TranslationTextComponent("message.no_speaker") : new StringTextComponent(getCurrentElement());
        int nameWidth = font.getStringWidth(name.getString());
        font.drawString(name.applyTextStyle(TextFormatting.WHITE).getString(), (float) (guiLeft + (xSize - nameWidth) / 2), guiTop + 7 + font.FONT_HEIGHT + 7, 0);
    }

}
