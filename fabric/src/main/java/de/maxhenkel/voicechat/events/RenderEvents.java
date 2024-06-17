package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class RenderEvents {

    public static final Event<ClientCompatibilityManager.RenderNameplateEvent> RENDER_NAMEPLATE = EventFactory.createArrayBacked(ClientCompatibilityManager.RenderNameplateEvent.class, (listeners) -> (entity, component, stack, vertexConsumers, light, partialTicks) -> {
        for (ClientCompatibilityManager.RenderNameplateEvent listener : listeners) {
            listener.render(entity, component, stack, vertexConsumers, light, partialTicks);
        }
    });

    public static final Event<Consumer<GuiGraphics>> RENDER_HUD = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (guiGraphics) -> {
        for (Consumer<GuiGraphics> listener : listeners) {
            listener.accept(guiGraphics);
        }
    });

}
