package de.maxhenkel.voicechat.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class PlayerStateMessage implements Message<PlayerStateMessage> {

    private UUID uuid;
    private PlayerState playerState;

    public PlayerStateMessage(UUID uuid, PlayerState playerState) {
        this.uuid = uuid;
        this.playerState = playerState;
    }

    public PlayerStateMessage() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    public void executeClientSide(NetworkEvent.Context context) {
        exec();
    }

    @OnlyIn(Dist.CLIENT)
    private void exec() {
        Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().onPlayerStatePacket(this);
    }

    @Override
    public PlayerStateMessage fromBytes(PacketBuffer buf) {
        uuid = buf.readUniqueId();
        playerState = new PlayerState(buf.readBoolean(), buf.readBoolean());

        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(uuid);
        buf.writeBoolean(playerState.isDisabled());
        buf.writeBoolean(playerState.isDisconnected());
    }
}
