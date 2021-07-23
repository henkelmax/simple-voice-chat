package de.maxhenkel.voicechat.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.UUID;

public class PlayerStateMessage implements Message<PlayerStateMessage> {

    private PlayerState playerState;

    public PlayerStateMessage(PlayerState playerState) {
        this.playerState = playerState;
    }

    public PlayerStateMessage() {

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
    public PlayerStateMessage fromBytes(FriendlyByteBuf buf) {
        playerState = PlayerState.fromBytes(buf);

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        playerState.toBytes(buf);
    }
}
