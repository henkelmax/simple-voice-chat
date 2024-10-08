package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.BuildConstants;

public class ProxyConfig {

    public ConfigEntry<Integer> port;
    public ConfigEntry<String> bindAddress;
    public ConfigEntry<String> voiceHost;
    public ConfigEntry<Boolean> allowPings;

    public ProxyConfig(ConfigBuilder builder) {
        builder.header(String.format("Simple Voice Chat proxy config v%s", BuildConstants.MOD_VERSION));

        port = builder
                .integerEntry("port", -1, -1, 65535,
                        "The port number to use for the voice chat communication.",
                        "Audio packets are always transmitted via the UDP protocol on the port number",
                        "specified here, independently of other networking used for the game server.",
                        "Set this to '-1' to use the same port number as the one used by the proxy."
                );

        bindAddress = builder
                .stringEntry("bind_address", "",
                        "The proxy IP address to bind the voice chat to",
                        "Leave blank to use the proxy bind address",
                        "To bind to the wildcard IP address, use '*'"
                );

        voiceHost = builder
                .stringEntry("voice_host", "",
                        "The hostname that clients should use to connect to the voice chat",
                        "This may also include a port, e.g. 'example.com:24454'",
                        "Do NOT change this value if you don't know what you're doing"
                );

        allowPings = builder
                .booleanEntry("allow_pings", true,
                        "If the voice chat proxy server should reply to external pings"
                );
    }

}
