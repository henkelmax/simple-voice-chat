package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.plugins.impl.GroupImpl;
import net.minecraft.network.PacketBuffer;

import java.util.UUID;

public class ClientGroup {

    private final UUID id;
    private final String name;
    private final boolean hasPassword;
    private final boolean persistent;
    private final de.maxhenkel.voicechat.api.Group.Type type;

    public ClientGroup(UUID id, String name, boolean hasPassword, boolean persistent, de.maxhenkel.voicechat.api.Group.Type type) {
        this.id = id;
        this.name = name;
        this.hasPassword = hasPassword;
        this.persistent = persistent;
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

    public Group.Type getType() {
        return type;
    }

    public static ClientGroup fromBytes(PacketBuffer buf) {
        return new ClientGroup(buf.readUUID(), buf.readUtf(512), buf.readBoolean(), buf.readBoolean(), GroupImpl.TypeImpl.fromInt(buf.readShort()));
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(id);
        buf.writeUtf(name, 512);
        buf.writeBoolean(hasPassword);
        buf.writeBoolean(persistent);
        buf.writeShort(GroupImpl.TypeImpl.toInt(type));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientGroup group = (ClientGroup) o;

        return id != null ? id.equals(group.id) : group.id == null;
    }
}
