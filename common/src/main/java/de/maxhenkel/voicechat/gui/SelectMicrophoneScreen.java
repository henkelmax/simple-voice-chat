package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ListScreen;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;

public class SelectMicrophoneScreen extends ListScreen<String> {

    protected int selected;

    public SelectMicrophoneScreen(Screen parent) {
        super(parent, MicrophoneManager.deviceNames(), new TranslationTextComponent("gui.voicechat.select_microphone.title"));
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
        Button b = addButton(new Button(width / 2 - bw / 2, guiTop + 35, bw, 20, new TranslationTextComponent("message.voicechat.select"), button -> {
            VoicechatClient.CLIENT_CONFIG.microphone.set(currentElement).save();
            button.active = false;
            ClientVoicechat client = ClientManager.getClient();
            if (client != null) {
                client.reloadAudio();
            }
        }));

        b.active = !currentElement.equals(VoicechatClient.CLIENT_CONFIG.microphone.get());
    }

    @Override
    protected void renderText(MatrixStack stack, @Nullable String element, int mouseX, int mouseY, float partialTicks) {
        ITextComponent title = getTitle();
        int titleWidth = font.width(title);
        font.draw(stack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, getFontColor());

        IFormattableTextComponent name = getCurrentElement() == null ? new TranslationTextComponent("message.voicechat.no_microphone") : new StringTextComponent(SoundManager.cleanDeviceName(getCurrentElement()));
        int nameWidth = font.width(name);
        font.draw(stack, name.withStyle(TextFormatting.WHITE).getVisualOrderText(), (float) (guiLeft + (xSize - nameWidth) / 2), guiTop + 7 + font.lineHeight + 7, 0);
    }
}
