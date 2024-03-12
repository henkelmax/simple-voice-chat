package de.maxhenkel.voicechat.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.BuildConstants;

public class ProxyConfig {

    public ConfigEntry<Integer> port;
    public ConfigEntry<String> bindAddress;
    public ConfigEntry<String> voiceHost;

    public ProxyConfig(ConfigBuilder builder) {
        builder.header(String.format("Simple Voice Chat proxy config v%s", BuildConstants.MOD_VERSION));

        port = builder
                .integerEntry("port", -1, -1, 65535,
                        "The port of the voice chat proxy server",
                        "Setting this to \"-1\" sets the port to the proxies port"
                );

        bindAddress = builder
                .stringEntry("bind_address", "",
                        "The IP address to bind the voice chat proxy server on",
                        "Leave empty to use the proxies bind address",
                        "To bind to the wildcard address, use '*'"
                );

        voiceHost = builder
                .stringEntry("voice_host", "",
                        "The host name that clients should use to connect to the voice chat",
                        "This may also include a port, e.g. 'example.com:24454'",
                        "Don't change this value if you don't know what you are doing"
                );
    }

}
