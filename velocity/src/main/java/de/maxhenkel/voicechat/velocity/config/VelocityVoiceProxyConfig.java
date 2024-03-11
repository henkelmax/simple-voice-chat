package de.maxhenkel.voicechat.velocity.config;


import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.annotations.Expose;
import com.velocitypowered.api.proxy.ProxyServer;
import de.maxhenkel.voicechat.proxy.config.VoiceProxyConfig;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Path;

public class VelocityVoiceProxyConfig implements VoiceProxyConfig  {
    /**
     * Which IP should the public UDP socket listen on
     */
    @Expose
    private final InetAddress ip;
    /**
     * Which port should the public UDP socket listen on
     */
    @Expose
    private final int port;

    private VelocityVoiceProxyConfig(
            InetAddress ip,
            int port
    ) {
        this.ip = ip;
        this.port = port;
    }

    public static VelocityVoiceProxyConfig read(ProxyServer proxyServer, Path path) throws IOException {
        URL defaultConfigLocation = VelocityVoiceProxyConfig.class.getClassLoader().getResource("default-config.toml");
        if (defaultConfigLocation == null) throw new RuntimeException("Missing default configuration in plugin.jar");

        CommentedFileConfig config = CommentedFileConfig.builder(path)
                .defaultData(defaultConfigLocation)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();
        config.load();

        String ipText = config.getOrElse("ip", proxyServer.getBoundAddress().getAddress().getHostAddress());
        InetAddress ip = InetAddress.getByName(ipText);

        int port = config.getOrElse("port", proxyServer.getBoundAddress().getPort());

        return new VelocityVoiceProxyConfig(ip, port);
    }

    @Override
    public InetAddress getAddress() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }
}
