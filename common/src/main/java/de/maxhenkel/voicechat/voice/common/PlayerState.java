package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerState {

    private UUID uuid;
    private String name;
    private boolean muted;
    private boolean disabled;
    private boolean disconnected;
    @Nullable
    private UUID group;

    public PlayerState(UUID uuid, String name, boolean muted, boolean disabled, boolean disconnected) {
        this.uuid = uuid;
        this.name = name;
        this.muted = muted;
        this.disabled = disabled;
        this.disconnected = disconnected;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
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

    @Nullable
    public UUID getGroup() {
        return group;
    }

    public void setGroup(@Nullable UUID group) {
        this.group = group;
    }

    public boolean hasGroup() {
        return group != null;
    }

    @Override
    public String toString() {
        return "{" +
                "muted=" + muted +
                ", disabled=" + disabled +
                ", disconnected=" + disconnected +
                ", uuid=" + uuid +
                ", name=" + name +
                ", group=" + group +
                '}';
    }

    public static PlayerState fromBytes(PacketBuffer buf) {
        boolean muted = buf.readBoolean();
        boolean disabled = buf.readBoolean();
        boolean disconnected = buf.readBoolean();
        UUID uuid = buf.readUniqueId();
        String name = buf.readString(32767);

        PlayerState state = new PlayerState(uuid, name, muted, disabled, disconnected);

        if (buf.readBoolean()) {
            state.setGroup(buf.readUniqueId());
        }

        return state;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(muted);
        buf.writeBoolean(disabled);
        buf.writeBoolean(disconnected);
        buf.writeUniqueId(uuid);
        buf.writeString(name);
        buf.writeBoolean(hasGroup());
        if (hasGroup()) {
            buf.writeUniqueId(group);
        }
    }

}
