package de.maxhenkel.voicechat.events;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.gui.GuiGraphics;
import org.quiltmc.qsl.base.api.event.Event;

import java.util.function.Consumer;

public class RenderEvents {

    public static final Event<ClientCompatibilityManager.RenderNameplateEvent> RENDER_NAMEPLATE = Event.create(ClientCompatibilityManager.RenderNameplateEvent.class, (listeners) -> (entity, component, stack, vertexConsumers, light) -> {
        for (ClientCompatibilityManager.RenderNameplateEvent listener : listeners) {
            listener.render(entity, component, stack, vertexConsumers, light);
        }
    });

    public static final Event<Consumer<GuiGraphics>> RENDER_HUD = Event.create(Consumer.class, (listeners) -> (guiGraphics) -> {
        for (Consumer<GuiGraphics> listener : listeners) {
            listener.accept(guiGraphics);
        }
    });

}
