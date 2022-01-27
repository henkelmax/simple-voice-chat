package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ListScreen;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.ClientVoicechat;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import de.maxhenkel.voicechat.voice.client.microphone.MicrophoneManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class SelectMicrophoneScreen extends ListScreen<String> {

    protected int selected;

    public SelectMicrophoneScreen(Screen parent) {
        super(parent, MicrophoneManager.deviceNames(), new TranslatableComponent("gui.voicechat.select_microphone.title"));
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
        Button b = addRenderableWidget(new Button(width / 2 - bw / 2, guiTop + 35, bw, 20, new TranslatableComponent("message.voicechat.select"), button -> {
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
    protected void renderText(PoseStack stack, @Nullable String element, int mouseX, int mouseY, float partialTicks) {
        Component title = getTitle();
        int titleWidth = font.width(title);
        font.draw(stack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, getFontColor());

        MutableComponent name = getCurrentElement() == null ? new TranslatableComponent("message.voicechat.no_microphone") : new TextComponent(SoundManager.cleanDeviceName(getCurrentElement()));
        int nameWidth = font.width(name);
        font.draw(stack, name.withStyle(ChatFormatting.WHITE).getVisualOrderText(), (float) (guiLeft + (xSize - nameWidth) / 2), guiTop + 7 + font.lineHeight + 7, 0);
    }
}
