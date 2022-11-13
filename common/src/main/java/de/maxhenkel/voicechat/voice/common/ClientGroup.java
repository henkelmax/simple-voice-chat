package de.maxhenkel.voicechat.voice.common;

import net.minecraft.network.PacketBuffer;

import java.util.UUID;

public class ClientGroup {

    private final UUID id;
    private final String name;
    private final boolean hasPassword;

    public ClientGroup(UUID id, String name, boolean hasPassword) {
        this.id = id;
        this.name = name;
        this.hasPassword = hasPassword;
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

    public static ClientGroup fromBytes(PacketBuffer buf) {
        return new ClientGroup(buf.readUniqueId(), buf.readString(512), buf.readBoolean());
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeUniqueId(id);
        buf.writeString(name);
        buf.writeBoolean(hasPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientGroup group = (ClientGroup) o;

        return id != null ? id.equals(group.id) : group.id == null;
    }
}
