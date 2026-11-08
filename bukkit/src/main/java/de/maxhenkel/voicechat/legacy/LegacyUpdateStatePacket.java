package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.net.UpdateStatePacket;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.entity.Player;

public class LegacyUpdateStatePacket implements Packet<LegacyUpdateStatePacket> {

    public static final Key UPDATE_STATE = Voicechat.compatibility.createNamespacedKey("update_state");

    private boolean disabled;

    public LegacyUpdateStatePacket() {

    }

    @Override
    public Key getID() {
        return UPDATE_STATE;
    }

    @Override
    public void onPacket(Player player) {
        Voicechat.SERVER.getServer().getPlayerStateManager().onUpdateStatePacket(player, new UpdateStatePacket(false, disabled));
    }

    @Override
    public LegacyUpdateStatePacket fromBytes(FriendlyByteBuf buf) {
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(disabled);
    }

}
