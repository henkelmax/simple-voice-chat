package de.maxhenkel.voicechat.events;

import net.minecraftforge.eventbus.api.GenericEvent;

public class VoiceChatDisconnectedEvent extends GenericEvent<VoiceChatDisconnectedEvent> {

    @Override
    public boolean isCancelable() {
        return false;
    }

}
