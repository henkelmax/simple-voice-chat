package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.gui.widgets.ToggleImageButton;
import de.maxhenkel.voicechat.voice.client.ClientPlayerStateManager;
import de.maxhenkel.voicechat.voice.client.MicrophoneActivationType;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;

public class VoiceChatScreen extends VoiceChatScreenBase {

    protected static final int FONT_COLOR = 4210752;

    private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_voicechat.png");
    private static final ResourceLocation MICROPHONE = new ResourceLocation(Main.MODID, "textures/gui/micrphone_button.png");
    private static final ResourceLocation HIDE = new ResourceLocation(Main.MODID, "textures/gui/hide_button.png");
    private static final ResourceLocation SPEAKER = new ResourceLocation(Main.MODID, "textures/gui/speaker_button.png");

    private ToggleImageButton mute;

    public VoiceChatScreen() {
        super(new TranslationTextComponent("gui.voicechat.voice_chat.title"), 195, 76);
    }

    @Override
    protected void init() {
        super.init();

        ClientPlayerStateManager stateManager = Main.CLIENT_VOICE_EVENTS.getPlayerStateManager();

        mute = new ToggleImageButton(guiLeft + 6, guiTop + ySize - 6 - 20, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslationTextComponent("message.voicechat.mute_microphone").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(mute);

        ToggleImageButton disable = new ToggleImageButton(guiLeft + 6 + 20 + 2, guiTop + ySize - 6 - 20, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslationTextComponent("message.voicechat.disable_voice_chat").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(disable);

        ToggleImageButton hide = new ToggleImageButton(guiLeft + xSize - 6 - 20, guiTop + ySize - 6 - 20, HIDE, Main.CLIENT_CONFIG.hideIcons::get, button -> {
            Main.CLIENT_CONFIG.hideIcons.set(!Main.CLIENT_CONFIG.hideIcons.get());
            Main.CLIENT_CONFIG.hideIcons.save();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(new TranslationTextComponent("message.voicechat.hide_icons").getVisualOrderText()), mouseX, mouseY);
        });
        addButton(hide);

        Button settings = new Button(guiLeft + 6, guiTop + 6 + 15, 75, 20, new StringTextComponent("Settings"), button -> {
            minecraft.setScreen(new VoiceChatSettingsScreen());
        });
        addButton(settings);

        Button group = new Button(guiLeft + xSize - 6 - 75 + 1, guiTop + 6 + 15, 75, 20, new StringTextComponent("Group"), button -> {
            if (stateManager.isInGroup()) {
                minecraft.setScreen(new GroupScreen());
            } else {
                minecraft.setScreen(new CreateGroupScreen());
            }
        });
        addButton(group);
        group.active = Main.SERVER_CONFIG.groupsEnabled.get();

        checkButtons();
    }

    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = Main.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        minecraft.getTextureManager().bind(TEXTURE);
        blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        ITextComponent title = new TranslationTextComponent("gui.voicechat.voice_chat.title");
        int titleWidth = font.width(title.getString());
        font.draw(matrixStack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }

}
