package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.ResourceLocation;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.bukkit.entity.Player;

public class PlayerStatePacket implements Packet<PlayerStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(Voicechat.MODID, "player_state");

    private PlayerState playerState;

    public PlayerStatePacket() {

    }

    public PlayerStatePacket(PlayerState playerState) {
        this.playerState = playerState;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    @Override
    public ResourceLocation getID() {
        return PLAYER_STATE;
    }

    @Override
    public void onPacket(Player player) {
        Voicechat.SERVER.getServer().getPlayerStateManager().onPlayerStatePacket(player, this);
    }

    @Override
    public PlayerStatePacket fromBytes(FriendlyByteBuf buf) {
        playerState = PlayerState.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        playerState.toBytes(buf);
    }

}
