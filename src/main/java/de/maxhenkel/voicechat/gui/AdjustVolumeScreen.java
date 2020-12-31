package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
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
    protected void renderText(MatrixStack stack, @Nullable PlayerInfo element, int mouseX, int mouseY, float partialTicks) {
        ITextComponent title = getCurrentElement() == null ? new TranslationTextComponent("message.no_player") : new TranslationTextComponent("message.adjust_volume_player", getCurrentElement().getName());
        int titleWidth = font.getStringPropertyWidth(title);
        font.func_238422_b_(stack, title.func_241878_f(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }
}
