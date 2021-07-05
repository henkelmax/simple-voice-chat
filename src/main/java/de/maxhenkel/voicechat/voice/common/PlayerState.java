package de.maxhenkel.voicechat.voice.common;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;

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
     * Empty strings will be treated as null
     *
     * @param group the group name (Max 16 characters)
     */
    public void setGroup(@Nullable String group) {
        if (group == null || group.isEmpty()) {
            this.group = null;
        } else {
            this.group = group;
        }
    }

    public boolean hasGroup() {
        return group != null;
    }

    public static PlayerState fromBytes(PacketBuffer buf) {
        PlayerState state = new PlayerState(buf.readBoolean(), buf.readBoolean(), NBTUtil.readGameProfile(buf.readNbt()));

        if (buf.readBoolean()) {
            state.setGroup(buf.readUtf(512));
        }

        return state;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(disabled);
        buf.writeBoolean(disconnected);
        buf.writeNbt(NBTUtil.writeGameProfile(new CompoundNBT(), gameProfile));
        buf.writeBoolean(hasGroup());
        if (hasGroup()) {
            buf.writeUtf(group, 512);
        }
    }
}
