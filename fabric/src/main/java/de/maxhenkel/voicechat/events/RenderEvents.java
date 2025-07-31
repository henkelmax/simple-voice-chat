package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class RenderEvents {

    public static final Event<ClientCompatibilityManager.RenderNameplateEvent> RENDER_NAMEPLATE = EventFactory.createArrayBacked(ClientCompatibilityManager.RenderNameplateEvent.class, (listeners) -> (uuid, discrete, component, pose, vertexConsumers, light) -> {
        for (ClientCompatibilityManager.RenderNameplateEvent listener : listeners) {
            listener.render(uuid, discrete, component, pose, vertexConsumers, light);
        }
    });

    public static final Event<Consumer<GuiGraphics>> RENDER_HUD = EventFactory.createArrayBacked(Consumer.class, (listeners) -> (guiGraphics) -> {
        for (Consumer<GuiGraphics> listener : listeners) {
            listener.accept(guiGraphics);
        }
    });

}
