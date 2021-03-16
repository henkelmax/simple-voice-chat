package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.events.IKeyBinding;
import de.maxhenkel.voicechat.events.KeyboardEvents;
import net.minecraft.client.util.InputUtil;

public class PTTKeyHandler implements KeyboardEvents.KeyEvent {

    private boolean pttKeyDown;

    public PTTKeyHandler() {
        KeyboardEvents.KEY.register(this);
    }

    @Override
    public void onKeyEvent(long window, int key, int scancode) {
        pttKeyDown = InputUtil.isKeyPressed(window, ((IKeyBinding) VoicechatClient.KEY_PTT).getBoundKey().getCode());
    }

    public boolean isPTTDown() {
        return pttKeyDown;
    }
}
