package de.maxhenkel.voicechat.api;

import java.net.SocketAddress;

public interface RawUdpPacket {

    byte[] getData();

    /**
     * This is currently unused by the mod, but it should still provide a correct value.
     *
     * @return the timestamp the packet has been received
     */
    long getTimestamp();

    SocketAddress getSocketAddress();

}
