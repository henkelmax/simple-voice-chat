package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class PlayerStatePacket implements Packet<PlayerStatePacket> {

    public static final Identifier PLAYER_STATE = new Identifier(Voicechat.MODID, "player_state");

    private UUID uuid;
    private PlayerState playerState;

    public PlayerStatePacket() {

    }

    public PlayerStatePacket(UUID uuid, PlayerState playerState) {
        this.uuid = uuid;
        this.playerState = playerState;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public Identifier getID() {
        return PLAYER_STATE;
    }

    @Override
    public PlayerStatePacket fromBytes(PacketByteBuf buf) {
        uuid = buf.readUuid();
        playerState = new PlayerState(buf.readBoolean(), buf.readBoolean());
        return this;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeBoolean(playerState.isDisabled());
        buf.writeBoolean(playerState.isDisconnected());
    }

}
