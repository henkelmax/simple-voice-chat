package de.maxhenkel.voicechat.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ClientVoiceChatDisconnectedEvent extends Event {

    @Override
    public boolean isCancelable() {
        return false;
    }

}
