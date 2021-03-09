package de.maxhenkel.voicechat.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class SetPlayerStateMessage implements Message<SetPlayerStateMessage> {

    private PlayerState playerState;

    public SetPlayerStateMessage(PlayerState playerState) {
        this.playerState = playerState;
    }

    public SetPlayerStateMessage() {

    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Server server = Main.SERVER_VOICE_EVENTS.getServer();
        if (server != null) {
            server.getPlayerStateManager().onPlayerStatePacket(context.getSender(), this);
        }
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    @Override
    public SetPlayerStateMessage fromBytes(PacketBuffer buf) {
        playerState = new PlayerState(buf.readBoolean(), buf.readBoolean());
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(playerState.isDisabled());
        buf.writeBoolean(playerState.isDisconnected());
    }
}
