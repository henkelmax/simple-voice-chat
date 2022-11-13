package de.maxhenkel.voicechat.events;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ServerVoiceChatConnectedEvent extends Event {

    private final EntityPlayerMP player;

    public ServerVoiceChatConnectedEvent(EntityPlayerMP player) {
        this.player = player;
    }

    public EntityPlayerMP getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
