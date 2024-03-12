package de.maxhenkel.voicechat;

import net.md_5.bungee.api.plugin.Plugin;

public class BungeecordPlugin extends Plugin {

    private SimpleVoiceChatBungeecord simpleVoiceChatBungeecord;

    @Override
    public void onEnable() {
        simpleVoiceChatBungeecord = new SimpleVoiceChatBungeecord(this);
        getProxy().getPluginManager().registerListener(this, simpleVoiceChatBungeecord);
        simpleVoiceChatBungeecord.onProxyInitialization();
    }

    @Override
    public void onDisable() {
        if (simpleVoiceChatBungeecord == null) {
            return;
        }
        getProxy().getPluginManager().unregisterListener(simpleVoiceChatBungeecord);
        simpleVoiceChatBungeecord.onProxyShutdown();
        simpleVoiceChatBungeecord = null;
    }
}
