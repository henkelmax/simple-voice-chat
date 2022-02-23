package de.maxhenkel.voicechat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeSlider;
import de.maxhenkel.voicechat.gui.widgets.ListScreen;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.List;

public class AdjustVolumeScreen extends ListScreen<PlayerState> {

    private static final Component TITLE = new TranslatableComponent("gui.voicechat.adjust_volume.title");
    private static final Component NO_PLAYER = new TranslatableComponent("message.voicechat.no_player");

    public AdjustVolumeScreen(Screen parent, List<PlayerState> players) {
        super(parent, players, TITLE);
    }

    @Override
    public void updateCurrentElement() {
        super.updateCurrentElement();
        PlayerState currentElement = getCurrentElement();
        if (currentElement == null) {
            return;
        }
        addRenderableWidget(new AdjustVolumeSlider(guiLeft + 10, guiTop + 30, xSize - 20, 20, currentElement.getUuid()));
    }

    @Override
    protected void renderText(PoseStack stack, @Nullable PlayerState element, int mouseX, int mouseY, float partialTicks) {
        Component title = getCurrentElement() == null ? NO_PLAYER : new TranslatableComponent("message.voicechat.adjust_volume_player", getCurrentElement().getName());
        int titleWidth = font.width(title);
        font.draw(stack, title.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);
    }
}
