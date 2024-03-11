package de.maxhenkel.voicechat.proxy.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.entry.ConfigEntry;
import de.maxhenkel.voicechat.BuildConstants;

public class ProxyConfig {

    public ConfigEntry<Integer> port;
    public ConfigEntry<String> bindAddress;

    public ProxyConfig(ConfigBuilder builder) {
        builder.header(String.format("Simple Voice Chat proxy config v%s", BuildConstants.MOD_VERSION));

        //TODO Use the proxies port if set to -1
        port = builder
                .integerEntry("port", 25577, -1, 65535, //TODO Set to -1
                        "The port of the voice chat proxy server",
                        "Setting this to \"-1\" sets the port to the proxies port"
                );

        //TODO Detect proxy bind address
        bindAddress = builder
                .stringEntry("bind_address", "",
                        "The IP address to bind the voice chat proxy server on",
                        "Leave empty to use the proxies bind address",
                        "To bind to the wildcard address, use '*'"
                );
    }

}
