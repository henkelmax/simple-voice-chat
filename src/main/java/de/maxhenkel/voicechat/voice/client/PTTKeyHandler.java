package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.Main;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PTTKeyHandler {

    private boolean pttKeyDown;

    public PTTKeyHandler() {

    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        InputConstants.Key boundKey = Main.KEY_PTT.getKey();
        if (boundKey.getValue() == -1 || boundKey.getType().equals(InputConstants.Type.MOUSE)) {
            return;
        }
        pttKeyDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), boundKey.getValue());
    }

    @SubscribeEvent
    public void onMouse(InputEvent.RawMouseEvent event) {
        InputConstants.Key boundKey = Main.KEY_PTT.getKey();
        if (boundKey.getValue() == -1 || !boundKey.getType().equals(InputConstants.Type.MOUSE)) {
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
