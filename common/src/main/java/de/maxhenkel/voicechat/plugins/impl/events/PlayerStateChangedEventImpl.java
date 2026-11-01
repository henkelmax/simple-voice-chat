package de.maxhenkel.voicechat.plugins.impl.events;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.PlayerStateChangedEvent;
import de.maxhenkel.voicechat.plugins.impl.VoicechatConnectionImpl;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerStateChangedEventImpl extends ServerEventImpl implements PlayerStateChangedEvent {

    protected final UUID playerUuid;
    protected final boolean muted;
    protected final boolean forceMuted;
    protected final boolean disabled;
    protected final boolean forceDisabled;
    protected final boolean disconnected;
    @Nullable
    protected VoicechatConnectionImpl connection;

    public PlayerStateChangedEventImpl(PlayerState state) {
        this.playerUuid = state.getUuid();
        this.muted = state.isMutedOrForceMuted();
        this.forceMuted = state.isForceMuted();
        this.disabled = state.isDisabledOrForceDisabled();
        this.forceDisabled = state.isForceDisabled();
        this.disconnected = state.isDisconnected();
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    @Override
    public boolean isForceMuted() {
        return forceMuted;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public boolean isForceDisabled() {
        return forceDisabled;
    }

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }

    @Override
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @Override
    @Nullable
    public VoicechatConnection getConnection() {
        if (connection == null) {
            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                return null;
            }
            ServerPlayerEntity player = server.getServer().getPlayerList().getPlayer(playerUuid);
            if (player == null) {
                return null;
            }
            connection = VoicechatConnectionImpl.fromPlayer(player);
        }
        return connection;
    }
}
