package de.maxhenkel.voicechat.util;

import java.net.SocketAddress;

public class BackendServer {

    private final String name;
    private final SocketAddress address;

    public BackendServer(String name, SocketAddress address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public SocketAddress getAddress() {
        return address;
    }

}
