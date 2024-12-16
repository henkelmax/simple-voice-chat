package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.config.PaperTranslations;
import de.maxhenkel.voicechat.config.Translations;
import de.maxhenkel.voicechat.integration.placeholderapi.VoicechatExpansion;
import de.maxhenkel.voicechat.integration.viaversion.ViaVersionCompatibility;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.PaperCommonCompatibilityManager;
import de.maxhenkel.voicechat.plugins.PluginManager;
import de.maxhenkel.voicechat.plugins.impl.BukkitVoicechatServiceImpl;
import io.papermc.paper.ServerBuildInfo;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.logging.Level;

public class VoicechatPaperPlugin extends JavaPlugin implements Listener {

    public static VoicechatPaperPlugin INSTANCE;
    public static BukkitVoicechatServiceImpl apiService;
    private Voicechat voicechat;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        try {
            if (!BuildConstants.MINECRAFT_VERSION.equals(ServerBuildInfo.buildInfo().minecraftVersionId())) {
                getLogger().severe("Incompatible Minecraft version - This plugin requires Minecraft %s".formatted(BuildConstants.MINECRAFT_VERSION));
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Failed to check Minecraft version", t);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        apiService = new BukkitVoicechatServiceImpl();
        getServer().getServicesManager().register(BukkitVoicechatService.class, apiService, this, ServicePriority.Normal);

        voicechat = new Voicechat() {
            @Override
            public Path getVoicechatConfigFolderInternal() {
                return getDataPath();
            }

            @Override
            protected void initPlugins() {
                //NOOP, this is initialized later so the apiService can gather all plugin registrations
            }

            @Override
            protected void registerCommands() {
                //NOOP, since commands need to get registered even earlier
            }

            @Override
            protected Translations createTranslations(ConfigBuilder builder) {
                return new PaperTranslations(builder);
            }
        };
        voicechat.initialize();

        getServer().getPluginManager().registerEvents((PaperCommonCompatibilityManager) CommonCompatibilityManager.INSTANCE, this);
        getServer().getPluginManager().registerEvents(this, this);

        try {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new VoicechatExpansion().register();
                Voicechat.LOGGER.info("Successfully registered PlaceholderAPI expansion");
            }
        } catch (Throwable t) {
            Voicechat.LOGGER.error("Failed to register PlaceholderAPI expansion", t);
        }

        try {
            if (Bukkit.getPluginManager().getPlugin("ViaVersion") != null) {
                ViaVersionCompatibility.register();
                Voicechat.LOGGER.info("Successfully added ViaVersion mappings");
            }
        } catch (Throwable t) {
            Voicechat.LOGGER.error("Failed to add ViaVersion mappings", t);
        }
    }

    @Override
    public void onDisable() {
        if (apiService != null) {
            getServer().getServicesManager().unregister(apiService);
        }
    }

    @EventHandler
    public void onServerStart(ServerLoadEvent event) {
        PluginManager.instance().init();
    }

}
