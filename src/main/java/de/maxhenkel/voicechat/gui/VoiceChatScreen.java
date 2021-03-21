package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Collections;

public class VoiceChatScreen extends VoiceChatScreenBase {

    private static final Identifier TEXTURE = new Identifier(Voicechat.MODID, "textures/gui/gui_voicechat.png");
    private static final Identifier MICROPHONE = new Identifier(Voicechat.MODID, "textures/gui/micrphone_button.png");
    private static final Identifier HIDE = new Identifier(Voicechat.MODID, "textures/gui/hide_button.png");
    private static final Identifier SPEAKER = new Identifier(Voicechat.MODID, "textures/gui/speaker_button.png");

    private ToggleImageButton mute;

    public VoiceChatScreen() {
        super(new TranslatableText("gui.voicechat.voice_chat.title"), 195, 76);
    }

    @Override
    protected void init() {
        super.init();

        ClientPlayerStateManager stateManager = VoicechatClient.CLIENT.getPlayerStateManager();

        mute = new ToggleImageButton(guiLeft + 6, guiTop + ySize - 6 - 20, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, (button, matrices, mouseX, mouseY) -> {
            renderOrderedTooltip(matrices, Collections.singletonList(new TranslatableText("message.voicechat.mute_microphone").asOrderedText()), mouseX, mouseY);
        });
        addButton(mute);

        ToggleImageButton disable = new ToggleImageButton(guiLeft + 6 + 20 + 2, guiTop + ySize - 6 - 20, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, (button, matrices, mouseX, mouseY) -> {
            renderOrderedTooltip(matrices, Collections.singletonList(new TranslatableText("message.voicechat.disable_voice_chat").asOrderedText()), mouseX, mouseY);
        });
        addButton(disable);

        ToggleImageButton hide = new ToggleImageButton(guiLeft + xSize - 6 - 20, guiTop + ySize - 6 - 20, HIDE, VoicechatClient.CLIENT_CONFIG.hideIcons::get, button -> {
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(!VoicechatClient.CLIENT_CONFIG.hideIcons.get());
            VoicechatClient.CLIENT_CONFIG.hideIcons.save();
        }, (button, matrices, mouseX, mouseY) -> {
            renderOrderedTooltip(matrices, Collections.singletonList(new TranslatableText("message.voicechat.hide_icons").asOrderedText()), mouseX, mouseY);
        });
        addButton(hide);

        ButtonWidget settings = new ButtonWidget(guiLeft + 6, guiTop + 6 + 15, 75, 20, new LiteralText("Settings"), button -> {
            client.openScreen(new VoiceChatSettingsScreen());
        });
        addButton(settings);

        ButtonWidget group = new ButtonWidget(guiLeft + xSize - 6 - 75 + 1, guiTop + 6 + 15, 75, 20, new LiteralText("Group"), button -> {
            if (stateManager.isInGroup()) {
                client.openScreen(new GroupScreen());
            } else {
                client.openScreen(new CreateGroupScreen());
            }
        });
        addButton(group);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = VoicechatClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        client.getTextureManager().bindTexture(TEXTURE);
        drawTexture(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        Text title = new TranslatableText("gui.voicechat.voice_chat.title");
        int titleWidth = textRenderer.getWidth(title);
        textRenderer.draw(matrixStack, title.asOrderedText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }

}
