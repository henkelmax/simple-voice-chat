package de.maxhenkel.voicechat.voice.common;

import com.mojang.authlib.GameProfile;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;

import javax.annotation.Nullable;

public class PlayerState {

    private boolean disabled;
    private boolean disconnected;
    private GameProfile gameProfile;
    @Nullable
    private ClientGroup group;

    public PlayerState(boolean disabled, boolean disconnected, GameProfile gameProfile) {
        this.disabled = disabled;
        this.disconnected = disconnected;
        this.gameProfile = gameProfile;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    @Nullable
    public ClientGroup getGroup() {
        return group;
    }

    public void setGroup(@Nullable ClientGroup group) {
        this.group = group;
    }

    public boolean hasGroup() {
        return group != null;
    }

    @Override
    public String toString() {
        return "{" +
                "disabled=" + disabled +
                ", disconnected=" + disconnected +
                ", uuid=" + gameProfile.getId() +
                ", group=" + group +
                '}';
    }

    public static PlayerState fromBytes(FriendlyByteBuf buf) {
        PlayerState state = new PlayerState(buf.readBoolean(), buf.readBoolean(), new GameProfile(buf.readUUID(), buf.readUtf()));

        if (buf.readBoolean()) {
            state.setGroup(ClientGroup.fromBytes(buf));
        }

        return state;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(disabled);
        buf.writeBoolean(disconnected);
        buf.writeUUID(gameProfile.getId());
        buf.writeUtf(gameProfile.getName());
        buf.writeBoolean(hasGroup());
        if (hasGroup()) {
            group.toBytes(buf);
        }
    }

}
