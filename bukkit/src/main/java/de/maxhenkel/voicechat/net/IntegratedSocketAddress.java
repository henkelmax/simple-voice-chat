package de.maxhenkel.voicechat.net;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;

public class IntegratedSocketAddress extends SocketAddress {

    private final UUID uuid;

    public IntegratedSocketAddress(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IntegratedSocketAddress that = (IntegratedSocketAddress) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("IntegratedSocketAddress{%s}", uuid);
    }
}
