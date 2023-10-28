package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.plugins.impl.GroupImpl;
import net.minecraft.network.PacketBuffer;

import java.util.Objects;
import java.util.UUID;

public class ClientGroup {

    private final UUID id;
    private final String name;
    private final boolean hasPassword;
    private final boolean persistent;
    private final boolean hidden;
    private final de.maxhenkel.voicechat.api.Group.Type type;

    public ClientGroup(UUID id, String name, boolean hasPassword, boolean persistent, boolean hidden, de.maxhenkel.voicechat.api.Group.Type type) {
        this.id = id;
        this.name = name;
        this.hasPassword = hasPassword;
        this.persistent = persistent;
        this.hidden = hidden;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean hasPassword() {
        return hasPassword;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isHidden() {
        return hidden;
    }

    public Group.Type getType() {
        return type;
    }

    public static ClientGroup fromBytes(PacketBuffer buf) {
        return new ClientGroup(buf.readUniqueId(), buf.readString(512), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), GroupImpl.TypeImpl.fromInt(buf.readShort()));
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(id);
        buf.writeString(name);
        buf.writeBoolean(hasPassword);
        buf.writeBoolean(persistent);
        buf.writeBoolean(hidden);
        buf.writeShort(GroupImpl.TypeImpl.toInt(type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClientGroup group = (ClientGroup) o;

        return Objects.equals(id, group.id);
    }
}
