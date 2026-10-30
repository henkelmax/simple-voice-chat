package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.Key;
import org.bukkit.entity.Player;

public class UpdateStatePacket implements Packet<UpdateStatePacket> {

    public static final Key PLAYER_STATE = Voicechat.compatibility.createNamespacedKey("update_state");

    private boolean muted;
    private boolean disabled;

    public UpdateStatePacket() {

    }

    public UpdateStatePacket(boolean muted, boolean disabled) {
        this.muted = muted;
        this.disabled = disabled;
    }

    public boolean isMuted() {
        return muted;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public Key getID() {
        return PLAYER_STATE;
    }

    @Override
    public void onPacket(Player player) {
        Voicechat.SERVER.getServer().getPlayerStateManager().onUpdateStatePacket(player, this);
    }

    @Override
    public UpdateStatePacket fromBytes(FriendlyByteBuf buf) {
        muted = buf.readBoolean();
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(muted);
        buf.writeBoolean(disabled);
    }

}
