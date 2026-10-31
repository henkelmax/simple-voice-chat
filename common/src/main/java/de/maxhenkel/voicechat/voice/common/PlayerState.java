package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerState {

    private UUID uuid;
    private String name;
    private boolean muted;
    private volatile boolean forceMuted;
    private boolean disabled;
    private volatile boolean forceDisabled;
    private boolean disconnected;
    private boolean speakPermission;
    private boolean listenPermission;
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

    public boolean isForceMuted() {
        return forceMuted;
    }

    public void setForceMuted(boolean forceMuted) {
        this.forceMuted = forceMuted;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isForceDisabled() {
        return forceDisabled;
    }

    public void setForceDisabled(boolean forceDisabled) {
        this.forceDisabled = forceDisabled;
    }

    public boolean isMutedOrForceMuted() {
        return muted || forceMuted;
    }

    public boolean isDisabledOrForceDisabled() {
        return disabled || forceDisabled;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }

    public boolean hasSpeakPermission() {
        return speakPermission;
    }

    public void setSpeakPermission(boolean speakPermission) {
        this.speakPermission = speakPermission;
    }

    public boolean hasListenPermission() {
        return listenPermission;
    }

    public void setListenPermission(boolean listenPermission) {
        this.listenPermission = listenPermission;
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
                ", forceMuted=" + forceMuted +
                ", disabled=" + disabled +
                ", forceDisabled=" + forceDisabled +
                ", disconnected=" + disconnected +
                ", uuid=" + uuid +
                ", name=" + name +
                ", group=" + group +
                '}';
    }

    private static final byte MUTED_OFFSET = 0;
    private static final byte FORCE_MUTED_OFFSET = 1;
    private static final byte DISABLED_OFFSET = 2;
    private static final byte FORCE_DISABLED_OFFSET = 3;
    private static final byte DISCONNECTED_OFFSET = 4;
    private static final byte HAS_GROUP_OFFSET = 5;

    public static PlayerState fromBytes(FriendlyByteBuf buf) {
        byte flags = buf.readByte();
        boolean muted = getFlag(flags, MUTED_OFFSET);
        boolean forceMuted = getFlag(flags, FORCE_MUTED_OFFSET);
        boolean disabled = getFlag(flags, DISABLED_OFFSET);
        boolean forceDisabled = getFlag(flags, FORCE_DISABLED_OFFSET);
        boolean disconnected = getFlag(flags, DISCONNECTED_OFFSET);
        boolean hasGroup = getFlag(flags, HAS_GROUP_OFFSET);
        UUID uuid = buf.readUUID();
        String name = buf.readUtf(256);

        PlayerState state = new PlayerState(uuid, name, muted, disabled, disconnected);
        state.setForceMuted(forceMuted);
        state.setForceDisabled(forceDisabled);

        if (hasGroup) {
            state.setGroup(buf.readUUID());
        }

        return state;
    }

    public void toBytes(FriendlyByteBuf buf) {
        byte flags = 0b0;
        flags = setFlag(flags, MUTED_OFFSET, muted);
        flags = setFlag(flags, FORCE_MUTED_OFFSET, forceMuted);
        flags = setFlag(flags, DISABLED_OFFSET, disabled);
        flags = setFlag(flags, FORCE_DISABLED_OFFSET, forceDisabled);
        flags = setFlag(flags, DISCONNECTED_OFFSET, disconnected);
        boolean hasGroup = hasGroup();
        flags = setFlag(flags, HAS_GROUP_OFFSET, hasGroup);
        buf.writeByte(flags);
        buf.writeUUID(uuid);
        buf.writeUtf(name, 256);
        if (hasGroup) {
            buf.writeUUID(group);
        }
    }

    private static boolean getFlag(byte flags, byte offset) {
        return (flags & (0b1 << offset)) != 0;
    }

    private static byte setFlag(byte flags, byte offset, boolean value) {
        if (value) {
            return (byte) (flags | (0b1 << offset));
        }
        return flags;
    }

}
