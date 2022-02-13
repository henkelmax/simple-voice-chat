package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.util.InputMappings;

public class PTTKeyHandler {

    private boolean pttKeyDown;
    private boolean whisperKeyDown;

    public PTTKeyHandler() {
        ClientCompatibilityManager.INSTANCE.onKeyboardEvent(this::onKeyboardEvent);
        ClientCompatibilityManager.INSTANCE.onMouseEvent(this::onMouseEvent);
    }

    public void onKeyboardEvent(long window, int key, int scancode) {
        InputMappings.Input pttKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_PTT);
        if (pttKey.getValue() != -1 && !pttKey.getType().equals(InputMappings.Type.MOUSE)) {
            pttKeyDown = InputMappings.isKeyDown(window, pttKey.getValue());
        }

        InputMappings.Input whisperKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_WHISPER);
        if (whisperKey.getValue() != -1 && !whisperKey.getType().equals(InputMappings.Type.MOUSE)) {
            whisperKeyDown = InputMappings.isKeyDown(window, whisperKey.getValue());
        }
    }

    public void onMouseEvent(long window, int button, int action, int mods) {
        InputMappings.Input pttKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_PTT);
        if (pttKey.getValue() != -1 && pttKey.getType().equals(InputMappings.Type.MOUSE) && pttKey.getValue() == button) {
            pttKeyDown = action != 0;
        }

        InputMappings.Input whisperKey = ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_WHISPER);
        if (whisperKey.getValue() != -1 && whisperKey.getType().equals(InputMappings.Type.MOUSE) && whisperKey.getValue() == button) {
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
