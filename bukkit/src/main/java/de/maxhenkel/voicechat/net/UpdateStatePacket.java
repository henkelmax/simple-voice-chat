package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.NamespacedKeyUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class UpdateStatePacket implements Packet<UpdateStatePacket> {

    public static final NamespacedKey PLAYER_STATE = NamespacedKeyUtil.voicechat("update_state");

    private boolean disconnected;
    private boolean disabled;

    public UpdateStatePacket() {

    }

    public UpdateStatePacket(boolean disconnected, boolean disabled) {
        this.disconnected = disconnected;
        this.disabled = disabled;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public NamespacedKey getID() {
        return PLAYER_STATE;
    }

    @Override
    public void onPacket(Player player) {
        Voicechat.SERVER.getServer().getPlayerStateManager().onUpdateStatePacket(player, this);
    }

    @Override
    public UpdateStatePacket fromBytes(FriendlyByteBuf buf) {
        disconnected = buf.readBoolean();
        disabled = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(disconnected);
        buf.writeBoolean(disabled);
    }

}
