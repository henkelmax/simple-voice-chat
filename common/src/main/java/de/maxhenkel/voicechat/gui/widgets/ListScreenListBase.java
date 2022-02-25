package de.maxhenkel.voicechat.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public class ListScreenListBase<T extends ListScreenEntryBase<T>> extends ContainerObjectSelectionList<T> {

    public ListScreenListBase(int width, int height, int x, int y, int size) {
        super(Minecraft.getInstance(), width, height, x, y, size);
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, float partialTicks) {
        double scale = minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int) ((double) getRowLeft() * scale), (int) ((double) (height - y1) * scale), (int) ((double) (getScrollbarPosition() + 6) * scale), (int) ((double) (height - (height - y1) - y0 - 4) * scale));
        super.render(poseStack, x, y, partialTicks);
        RenderSystem.disableScissor();
    }

}
