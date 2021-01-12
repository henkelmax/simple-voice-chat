package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.PlayerInfo;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;

public class AdjustVolumeScreen extends ListScreen<PlayerInfo> {

    public AdjustVolumeScreen(List<PlayerInfo> players) {
        super(players, new TranslationTextComponent("gui.adjust_volume.title"));
    }

    @Override
    public void updateCurrentElement() {
        super.updateCurrentElement();
        PlayerInfo currentElement = getCurrentElement();
        if (currentElement == null) {
            return;
        }
        addButton(new AdjustVolumeSlider(guiLeft + 10, guiTop + 30, xSize - 20, 20, currentElement));
    }

    @Override
    protected void renderText(@Nullable PlayerInfo element, int mouseX, int mouseY, float partialTicks) {
        ITextComponent title = getCurrentElement() == null ? new TranslationTextComponent("message.no_player") : new TranslationTextComponent("message.adjust_volume_player", getCurrentElement().getName());
        int titleWidth = font.getStringWidth(title.getString());
        font.drawString(title.getString(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }
}
