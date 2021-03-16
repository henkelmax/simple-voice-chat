package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.events.KeyEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.util.InputUtil;

public class PTTKeyHandler implements KeyEvents.KeyboardEvent, KeyEvents.MouseEvent {

    private boolean pttKeyDown;

    public PTTKeyHandler() {
        KeyEvents.KEYBOARD_KEY.register(this);
        KeyEvents.MOUSE_KEY.register(this);
    }

    @Override
    public void onKeyboardEvent(long window, int key, int scancode) {
        InputUtil.Key boundKey = KeyBindingHelper.getBoundKeyOf(VoicechatClient.KEY_PTT);
        if (boundKey.getCategory().equals(InputUtil.Type.MOUSE)) {
            return;
        }
        pttKeyDown = InputUtil.isKeyPressed(window, boundKey.getCode());
    }

    @Override
    public void onMouseEvent(long window, int button, int action, int mods) {
        InputUtil.Key boundKey = KeyBindingHelper.getBoundKeyOf(VoicechatClient.KEY_PTT);
        if (!boundKey.getCategory().equals(InputUtil.Type.MOUSE)) {
            return;
        }
        if (boundKey.getCode() != button) {
            return;
        }
        pttKeyDown = action != 0;
    }

    public boolean isPTTDown() {
        return pttKeyDown;
    }

}
