package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PTTKeyHandler {

    private boolean pttKeyDown;

    public PTTKeyHandler() {

    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        InputMappings.Input boundKey = Main.KEY_PTT.getKey();
        if (boundKey.getType().equals(InputMappings.Type.MOUSE)) {
            return;
        }
        pttKeyDown = InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), boundKey.getValue());
    }

    @SubscribeEvent
    public void onMouse(InputEvent.RawMouseEvent event) {
        InputMappings.Input boundKey = Main.KEY_PTT.getKey();
        if (!boundKey.getType().equals(InputMappings.Type.MOUSE)) {
            return;
        }
        if (boundKey.getValue() != event.getButton()) {
            return;
        }
        pttKeyDown = event.getAction() != 0;
    }

    public boolean isPTTDown() {
        return pttKeyDown;
    }
}
