package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.Packet;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.Key;
import de.maxhenkel.voicechat.voice.common.PlayerState;

public class LegacyPlayerStatePacket implements Packet<LegacyPlayerStatePacket> {

    public static final Key PLAYER_STATE = Voicechat.compatibility.createNamespacedKey("player_state");

    private PlayerState playerState;

    public LegacyPlayerStatePacket() {

    }

    public LegacyPlayerStatePacket(PlayerState playerState) {
        this.playerState = playerState;
    }

    @Override
    public Key getID() {
        return PLAYER_STATE;
    }

    @Override
    public LegacyPlayerStatePacket fromBytes(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Legacy packets can only be sent");
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        LegacyProtocolV20.writePlayerState(buf, playerState);
    }

}
