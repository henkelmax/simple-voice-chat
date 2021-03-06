package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class PlayerStatePacket {

    private UUID uuid;
    private PlayerState playerState;

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

    public static PlayerStatePacket fromBytes(PacketByteBuf buf) {
        return new PlayerStatePacket(buf.readUuid(), new PlayerState(buf.readBoolean(), buf.readBoolean()));
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeBoolean(playerState.isDisabled());
        buf.writeBoolean(playerState.isDisconnected());
    }

}
