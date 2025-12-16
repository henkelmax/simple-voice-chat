package de.maxhenkel.voicechat.voice.common;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerState {

    private UUID uuid;
    private String name;
    private boolean disabled;
    private boolean disconnected;
    @Nullable
    private UUID group;

    public PlayerState(UUID uuid, String name, boolean disabled, boolean disconnected) {
        this.uuid = uuid;
        this.name = name;
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
                "disabled=" + disabled +
                ", disconnected=" + disconnected +
                ", uuid=" + uuid +
                ", name=" + name +
                ", group=" + group +
                '}';
    }

    public static PlayerState fromBytes(ByteBuf buf) {
        boolean disabled = buf.readBoolean();
        boolean disconnected = buf.readBoolean();
        UUID uuid = BufferUtils.readUUID(buf);
        String name = BufferUtils.readUtf(buf);

        PlayerState state = new PlayerState(uuid, name, disabled, disconnected);

        if (buf.readBoolean()) {
            state.setGroup(BufferUtils.readUUID(buf));
        }

        return state;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(disabled);
        buf.writeBoolean(disconnected);
        BufferUtils.writeUUID(buf, uuid);
        BufferUtils.writeUtf(buf, name);
        buf.writeBoolean(hasGroup());
        if (hasGroup()) {
            BufferUtils.writeUUID(buf, group);
        }
    }

}
