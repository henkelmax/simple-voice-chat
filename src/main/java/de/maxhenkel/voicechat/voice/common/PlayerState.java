package de.maxhenkel.voicechat.voice.common;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;

public class PlayerState {

    private boolean disabled;
    private boolean disconnected;
    private GameProfile gameProfile;
    @Nullable
    private String group;

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
    public String getGroup() {
        return group;
    }

    /**
     * @param group the group name (Max 16 characters)
     */
    public void setGroup(@Nullable String group) {
        this.group = group;
    }

    public boolean hasGroup() {
        return group != null;
    }

    public static PlayerState fromBytes(FriendlyByteBuf buf) {
        PlayerState state = new PlayerState(buf.readBoolean(), buf.readBoolean(), new GameProfile(buf.readUUID(), buf.readUtf()));

        if (buf.readBoolean()) {
            state.setGroup(buf.readUtf(512));
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
            buf.writeUtf(group, 512);
        }
    }

}
