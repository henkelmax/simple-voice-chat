package de.maxhenkel.voicechat.proxy.config;

import java.net.InetAddress;

/**
 * Defines which configuration options a proxy must support at the minimum
 */
public interface VoiceProxyConfig {

    /**
     * Returns the address on which the public-facing proxy UDP socket should be listening on
     * <p>
     * If not configured, this defaults to the proxy's TCP game socket address.
     */
    InetAddress getAddress();

    /**
     * Returns the port on which the public-facing proxy UDP socket should be listening on.
     * <p>
     * If not configured, this defaults to the proxy's TCP game socket port.
     */
    int getPort();

}
