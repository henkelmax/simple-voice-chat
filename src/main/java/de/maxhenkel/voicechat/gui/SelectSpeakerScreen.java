package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.client.DataLines;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import javax.annotation.Nullable;

public class SelectSpeakerScreen extends ListScreen<String> {

    protected int selected;

    public SelectSpeakerScreen() {
        super(DataLines.getSpeakerNames(), new TranslatableText("gui.voicechat.select_speaker.title"));
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i);
            if (element.equals(VoicechatClient.CLIENT_CONFIG.speaker.get())) {
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
        ButtonWidget b = addButton(new ButtonWidget(width / 2 - bw / 2, guiTop + 35, bw, 20, new TranslatableText("message.voicechat.select"), button -> {
            VoicechatClient.CLIENT_CONFIG.speaker.set(currentElement);
            VoicechatClient.CLIENT_CONFIG.speaker.save();
            button.active = false;
            Client client = VoicechatClient.CLIENT.getClient();
            if (client != null) {
                client.reloadDataLines();
            }
        }));

        b.active = !currentElement.equals(VoicechatClient.CLIENT_CONFIG.speaker.get());
    }

    @Override
    protected void renderText(MatrixStack stack, @Nullable String element, int mouseX, int mouseY, float partialTicks) {
        Text title = getTitle();
        int titleWidth = textRenderer.getWidth(title);
        textRenderer.draw(stack, title.asOrderedText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);

        MutableText name = getCurrentElement() == null ? new TranslatableText("message.voicechat.no_speaker") : new LiteralText(getCurrentElement());
        int nameWidth = textRenderer.getWidth(name);
        textRenderer.draw(stack, name.formatted(Formatting.WHITE).asOrderedText(), (float) (guiLeft + (xSize - nameWidth) / 2), guiTop + 7 + textRenderer.fontHeight + 7, 0);
    }
}
