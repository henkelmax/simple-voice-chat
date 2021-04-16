package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.gui.widgets.AdjustVolumeSlider;
import de.maxhenkel.voicechat.gui.widgets.ListScreen;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;
import java.util.List;

public class AdjustVolumeScreen extends ListScreen<PlayerState> {

    public AdjustVolumeScreen(Screen parent, List<PlayerState> players) {
        super(parent, players, new TranslatableText("gui.voicechat.adjust_volume.title"));
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
        Text title = getCurrentElement() == null ? new TranslatableText("message.voicechat.no_player") : new TranslatableText("message.voicechat.adjust_volume_player", getCurrentElement().getGameProfile().getName());
        int titleWidth = textRenderer.getWidth(title);
        textRenderer.draw(stack, title.asOrderedText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }
}
