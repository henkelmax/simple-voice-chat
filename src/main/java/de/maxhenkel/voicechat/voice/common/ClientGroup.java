package de.maxhenkel.voicechat.voice.common;

import de.maxhenkel.voicechat.util.FriendlyByteBuf;

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

    public static ClientGroup fromBytes(FriendlyByteBuf buf) {
        return new ClientGroup(buf.readUUID(), buf.readUtf(512), buf.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(id);
        buf.writeUtf(name, 512);
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