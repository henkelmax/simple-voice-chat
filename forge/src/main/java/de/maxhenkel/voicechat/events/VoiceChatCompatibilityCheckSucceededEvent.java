package de.maxhenkel.voicechat.events;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Event;

public class VoiceChatCompatibilityCheckSucceededEvent extends Event {

    private final EntityPlayerMP player;

    public VoiceChatCompatibilityCheckSucceededEvent(EntityPlayerMP player) {
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
