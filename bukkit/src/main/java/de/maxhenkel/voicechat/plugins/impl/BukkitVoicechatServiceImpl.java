package de.maxhenkel.voicechat.plugins.impl;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatPlugin;

import java.util.ArrayList;
import java.util.List;

public class BukkitVoicechatServiceImpl implements BukkitVoicechatService {

    private final List<VoicechatPlugin> plugins;

    public BukkitVoicechatServiceImpl() {
        plugins = new ArrayList<>();
    }

    @Override
    public void registerPlugin(VoicechatPlugin plugin) {
        plugins.add(plugin);
    }

    public List<VoicechatPlugin> getPlugins() {
        return plugins;
    }
}
