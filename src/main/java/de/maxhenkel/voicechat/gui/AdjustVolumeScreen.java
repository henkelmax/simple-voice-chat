package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxhenkel.voicechat.gui.widgets.AdjustVolumeSlider;
import de.maxhenkel.voicechat.gui.widgets.ListScreen;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;

public class AdjustVolumeScreen extends ListScreen<PlayerState> {

    public AdjustVolumeScreen(Screen parent, List<PlayerState> players) {
        super(parent, players, new TranslationTextComponent("gui.voicechat.adjust_volume.title"));
    }

    @Override
    public void updateCurrentElement() {
        super.updateCurrentElement();
        PlayerState currentElement = getCurrentElement();
        if (currentElement == null) {
            return;
        }
        addButton(new AdjustVolumeSlider(guiLeft + 10, guiTop + 30, xSize - 20, 20, currentElement));
    }

    @Override
    protected void renderText(MatrixStack stack, @Nullable PlayerState element, int mouseX, int mouseY, float partialTicks) {
        ITextComponent title = getCurrentElement() == null ? new TranslationTextComponent("message.voicechat.no_player") : new TranslationTextComponent("message.voicechat.adjust_volume_player", getCurrentElement().getGameProfile().getName());
        int titleWidth = font.width(title);
        font.draw(stack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }
}
