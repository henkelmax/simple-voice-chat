package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.*;
import de.maxhenkel.voicechat.voice.client.Client;
import de.maxhenkel.voicechat.voice.common.Utils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class VoiceChatSettingsScreen extends VoiceChatScreenBase implements MicTestButton.MicListener {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Voicechat.MODID, "textures/gui/gui_voicechat_settings.png");

    private double micValue;

    private VoiceActivationSlider voiceActivationSlider;

    public VoiceChatSettingsScreen() {
        super(new TranslatableComponent("gui.voicechat.voice_chat_settings.title"), 248, 226);
    }

    @Override
    protected void init() {
        super.init();

        voiceActivationSlider = new VoiceActivationSlider(guiLeft + 10, guiTop + 95, xSize - 20, 20);
        addButton(new VoiceSoundSlider(guiLeft + 10, guiTop + 20, xSize - 20, 20));
        addButton(new MicAmplificationSlider(guiLeft + 10, guiTop + 45, xSize - 20, 20));
        addButton(new MicActivationButton(guiLeft + 10, guiTop + 70, xSize - 20, 20, voiceActivationSlider));
        addButton(voiceActivationSlider);
        Client c = VoicechatClient.CLIENT.getClient();
        if (c != null) {
            addButton(new MicTestButton(guiLeft + 10, guiTop + 145, xSize - 20, 20, this, c));
        }
        addButton(new Button(guiLeft + 10, guiTop + 170, xSize - 20, 20, new TranslatableComponent("message.voicechat.adjust_volumes").getColoredString(), button -> {
            minecraft.setScreen(new AdjustVolumeScreen(this, VoicechatClient.CLIENT.getPlayerStateManager().getPlayerStates()));
        }));
        addButton(new Button(guiLeft + 10, guiTop + 195, xSize / 2 - 15, 20, new TranslatableComponent("message.voicechat.select_microphone").getColoredString(), button -> {
            minecraft.setScreen(new SelectMicrophoneScreen(this));
        }));
        addButton(new Button(guiLeft + xSize / 2 + 6, guiTop + 195, xSize / 2 - 15, 20, new TranslatableComponent("message.voicechat.select_speaker").getColoredString(), button -> {
            minecraft.setScreen(new SelectSpeakerScreen(this));
        }));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);

        blit(guiLeft + 10, guiTop + 120, 0, 244, xSize - 20, 20, 512, 512);
        blit(guiLeft + 11, guiTop + 121, 0, 226, (int) ((xSize - 18) * micValue), 18, 512, 512);

        int pos = (int) ((xSize - 20) * Utils.dbToPerc(VoicechatClient.CLIENT_CONFIG.voiceActivationThreshold.get()));

        blit(guiLeft + 10 + pos, guiTop + 120, 0, 244, 1, 20, 512, 512);

        super.render(mouseX, mouseY, partialTicks);

        Component title = new TranslatableComponent("gui.voicechat.voice_chat_settings.title");
        int titleWidth = font.width(title.getColoredString());
        font.draw(title.getColoredString(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }

    @Override
    public void onMicValue(double perc) {
        this.micValue = perc;
    }
}
