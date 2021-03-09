package de.maxhenkel.voicechat.event;

import net.minecraftforge.eventbus.api.Event;

public class VoiceChatDisconnectedEvent extends Event {

    @Override
    public boolean isCancelable() {
        return false;
    }

}
