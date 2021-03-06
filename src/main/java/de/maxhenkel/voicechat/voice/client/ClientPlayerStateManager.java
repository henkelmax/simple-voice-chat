package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.events.ClientVoiceChatEvents;
import de.maxhenkel.voicechat.events.ClientWorldEvents;
import de.maxhenkel.voicechat.net.Packets;
import de.maxhenkel.voicechat.net.PlayerStatePacket;
import de.maxhenkel.voicechat.net.PlayerStatesPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientPlayerStateManager {

    private boolean muted;
    private PlayerState state;
    private Map<UUID, PlayerState> states;

    public ClientPlayerStateManager() {
        muted = VoicechatClient.CLIENT_CONFIG.muted.get();
        state = new PlayerState(VoicechatClient.CLIENT_CONFIG.disabled.get(), true);
        states = new HashMap<>();
        ClientPlayNetworking.registerGlobalReceiver(Packets.PLAYER_STATE, (mc, handler, buf, responseSender) -> {
            PlayerStatePacket packet = PlayerStatePacket.fromBytes(buf);
            mc.execute(() -> {
                states.put(packet.getUuid(), packet.getPlayerState());
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(Packets.PLAYER_STATES, (mc, handler, buf, responseSender) -> {
            PlayerStatesPacket packet = PlayerStatesPacket.fromBytes(buf);
            mc.execute(() -> {
                states = packet.getPlayerStates();
            });
        });
        ClientVoiceChatEvents.VOICECHAT_CONNECTED.register(this::onVoiceChatConnected);
        ClientVoiceChatEvents.VOICECHAT_DISCONNECTED.register(this::onVoiceChatDisconnected);
        ClientWorldEvents.DISCONNECT.register(this::onDisconnect);
    }

    /**
     * Called when the voicechat client gets disconnected or the player logs out
     */
    public void onVoiceChatDisconnected() {
        state.setDisconnected(true);
        syncOwnState();

    }

    /**
     * Called when the voicechat client gets (re)connected
     */
    public void onVoiceChatConnected(Client client) {
        state.setDisconnected(false);
        syncOwnState();
    }

    private void onDisconnect() {
        clearStates();
    }

    public boolean isPlayerDisabled(PlayerEntity player) {
        PlayerState playerState = states.get(player.getUuid());
        if (playerState == null) {
            return false;
        }

        return playerState.isDisabled();
    }

    public boolean isPlayerDisconnected(PlayerEntity player) {
        PlayerState playerState = states.get(player.getUuid());
        if (playerState == null) {
            return true;
        }

        return playerState.isDisconnected();
    }

    public void syncOwnState() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        new PlayerStatePacket(new UUID(0L, 0L), state).toBytes(buf);
        ClientPlayNetworking.send(Packets.PLAYER_STATE, buf);
    }

    public boolean isDisabled() {
        return state.isDisabled();
    }

    public boolean isDisconnected() {
        return state.isDisconnected();
    }

    public void setDisabled(boolean disabled) {
        state.setDisabled(disabled);
        VoicechatClient.CLIENT_CONFIG.disabled.set(disabled);
        VoicechatClient.CLIENT_CONFIG.disabled.save();
        syncOwnState();
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        VoicechatClient.CLIENT_CONFIG.muted.set(muted);
        VoicechatClient.CLIENT_CONFIG.muted.save();
    }

    public void clearStates() {
        states.clear();
    }
}
