package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.PaperCommonCompatibilityManager;
import de.maxhenkel.voicechat.plugins.impl.BukkitVoicechatServiceImpl;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class VoicechatPaperPlugin extends JavaPlugin {

    public static VoicechatPaperPlugin INSTANCE;
    public static BukkitVoicechatServiceImpl apiService;
    private Voicechat voicechat;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        apiService = new BukkitVoicechatServiceImpl();
        getServer().getServicesManager().register(BukkitVoicechatService.class, apiService, this, ServicePriority.Normal);

        voicechat = new Voicechat() {
            @Override
            public Path getVoicechatConfigFolderInternal() {
                return getDataPath();
            }

            @Override
            protected void registerCommands() {
                //NOOP, since commands need to get registered even earlier
            }
        };
        voicechat.initialize();

        getServer().getPluginManager().registerEvents((PaperCommonCompatibilityManager) CommonCompatibilityManager.INSTANCE, this);
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregister(apiService);
    }

}
