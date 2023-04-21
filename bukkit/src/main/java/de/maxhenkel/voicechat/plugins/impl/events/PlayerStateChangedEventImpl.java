package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.PlayerStateChangedEvent;
import de.maxhenkel.voicechat.plugins.impl.VoicechatConnectionImpl;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerStateChangedEventImpl extends ServerEventImpl implements PlayerStateChangedEvent {

    protected final PlayerState state;
    @Nullable
    protected VoicechatConnectionImpl connection;

    public PlayerStateChangedEventImpl(PlayerState state) {
        this.state = state;
    }

    @Override
    public boolean isDisabled() {
        return state.isDisabled();
    }

    @Override
    public boolean isDisconnected() {
        return state.isDisconnected();
    }

    @Override
    public UUID getPlayerUuid() {
        return state.getUuid();
    }

    @Override
    @Nullable
    public VoicechatConnection getConnection() {
        if (connection == null) {
            Player player = Voicechat.INSTANCE.getServer().getPlayer(state.getUuid());
            if (player == null) {
                return null;
            }
            connection = VoicechatConnectionImpl.fromPlayer(player);
        }
        return connection;
    }
}
