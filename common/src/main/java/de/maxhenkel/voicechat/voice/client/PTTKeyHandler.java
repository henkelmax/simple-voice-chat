package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;

public class PTTKeyHandler {

    private boolean pttKeyDown;

    public PTTKeyHandler() {
        ClientCompatibilityManager.INSTANCE.onKeyboardEvent(this::onKeyboardEvent);
        ClientCompatibilityManager.INSTANCE.onMouseEvent(this::onMouseEvent);
    }

    public void onKeyboardEvent(long window, int key, int scancode) {
        InputConstants.Key boundKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_PTT);
        if (boundKey.getValue() == -1 || boundKey.getType().equals(InputConstants.Type.MOUSE)) {
            return;
        }
        pttKeyDown = InputConstants.isKeyDown(window, boundKey.getValue());
    }

    public void onMouseEvent(long window, int button, int action, int mods) {
        InputConstants.Key boundKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_PTT);
        if (boundKey.getValue() == -1 || !boundKey.getType().equals(InputConstants.Type.MOUSE)) {
            return;
        }
        if (boundKey.getValue() != button) {
            return;
        }
        pttKeyDown = action != 0;
    }

    public boolean isPTTDown() {
        return pttKeyDown;
    }

}
