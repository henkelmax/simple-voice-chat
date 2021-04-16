package de.maxhenkel.voicechat.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.voicechat.Main;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatesMessage implements Message<PlayerStatesMessage> {

    private Map<UUID, PlayerState> playerStates;

    public PlayerStatesMessage(Map<UUID, PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    public PlayerStatesMessage() {

    }

    public Map<UUID, PlayerState> getPlayerStates() {
        return playerStates;
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
        Main.CLIENT_VOICE_EVENTS.getPlayerStateManager().onPlayerStatesPacket(this);
    }

    @Override
    public PlayerStatesMessage fromBytes(PacketBuffer buf) {
        int count = buf.readInt();
        this.playerStates = new HashMap<>();
        for (int i = 0; i < count; i++) {
            PlayerState playerState = PlayerState.fromBytes(buf);
            playerStates.put(playerState.getGameProfile().getId(), playerState);
        }

        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(playerStates.size());
        for (Map.Entry<UUID, PlayerState> entry : playerStates.entrySet()) {
            entry.getValue().toBytes(buf);
        }
    }
}
