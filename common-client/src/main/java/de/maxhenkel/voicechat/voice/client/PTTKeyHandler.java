package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;

public class PTTKeyHandler {

    private boolean pttKeyDown;
    private boolean whisperKeyDown;

    public PTTKeyHandler() {
        ClientCompatibilityManager.INSTANCE.onKeyboardEvent(this::onKeyboardEvent);
        ClientCompatibilityManager.INSTANCE.onMouseEvent(this::onMouseEvent);
    }

    public void onKeyboardEvent(long window, int key, int scancode) {
        InputConstants.Key pttKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_PTT);
        if (pttKey.getValue() != -1 && !pttKey.getType().equals(InputConstants.Type.MOUSE)) {
            pttKeyDown = InputConstants.isKeyDown(window, pttKey.getValue());
        }

        InputConstants.Key whisperKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_WHISPER);
        if (whisperKey.getValue() != -1 && !whisperKey.getType().equals(InputConstants.Type.MOUSE)) {
            whisperKeyDown = InputConstants.isKeyDown(window, whisperKey.getValue());
        }
    }

    public void onMouseEvent(long window, int button, int action, int mods) {
        InputConstants.Key pttKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_PTT);
        if (pttKey.getValue() != -1 && pttKey.getType().equals(InputConstants.Type.MOUSE) && pttKey.getValue() == button) {
            pttKeyDown = action != 0;
        }

        InputConstants.Key whisperKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_WHISPER);
        if (whisperKey.getValue() != -1 && whisperKey.getType().equals(InputConstants.Type.MOUSE) && whisperKey.getValue() == button) {
            whisperKeyDown = action != 0;
        }
    }

    public boolean isPTTDown() {
        return pttKeyDown;
    }

    public boolean isWhisperDown() {
        return whisperKeyDown;
    }

    public boolean isAnyDown() {
        return pttKeyDown || whisperKeyDown;
    }

}
