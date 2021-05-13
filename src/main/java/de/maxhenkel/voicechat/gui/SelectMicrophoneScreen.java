package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ListScreen;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.client.DataLines;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class SelectMicrophoneScreen extends ListScreen<String> {

    protected int selected;

    public SelectMicrophoneScreen(Screen parent) {
        super(parent, DataLines.getMicrophoneNames(), new TranslatableComponent("gui.voicechat.select_microphone.title"));
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i);
            if (element.equals(VoicechatClient.CLIENT_CONFIG.microphone.get())) {
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
        Button b = addButton(new Button(width / 2 - bw / 2, guiTop + 35, bw, 20, new TranslatableComponent("message.voicechat.select").getColoredString(), button -> {
            VoicechatClient.CLIENT_CONFIG.microphone.set(currentElement);
            VoicechatClient.CLIENT_CONFIG.microphone.save();
            button.active = false;
            Client client = VoicechatClient.CLIENT.getClient();
            if (client != null) {
                client.reloadDataLines();
            }
        }));

        b.active = !currentElement.equals(VoicechatClient.CLIENT_CONFIG.microphone.get());
    }

    @Override
    protected void renderText(@Nullable String element, int mouseX, int mouseY, float partialTicks) {
        Component title = getTitle();
        int titleWidth = font.width(title.getColoredString());
        font.draw(title.getColoredString(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);

        Component name = getCurrentElement() == null ? new TranslatableComponent("message.voicechat.no_microphone") : new TextComponent(getCurrentElement());
        int nameWidth = font.width(name.getColoredString());
        font.draw(name.withStyle(ChatFormatting.WHITE).getColoredString(), (float) (guiLeft + (xSize - nameWidth) / 2), guiTop + 7 + font.lineHeight + 7, 0);
    }
}
