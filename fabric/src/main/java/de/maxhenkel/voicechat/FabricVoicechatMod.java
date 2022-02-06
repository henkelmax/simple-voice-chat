package de.maxhenkel.voicechat;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.voicechat.config.FabricServerConfig;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.FabricCommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.FabricPermissionManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class FabricVoicechatMod extends Voicechat implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER_CONFIG = ConfigBuilder.build(server.getServerDirectory().toPath().resolve("config").resolve(MODID).resolve("voicechat-server.properties"), true, FabricServerConfig::new);
        });

        initialize();
    }

    @Override
    public int readCompatibilityVersion() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MODID).orElse(null);
        if (modContainer == null) {
            return -1;
        }
        return modContainer.getMetadata().getCustomValue(MODID).getAsObject().get("compatibilityVersion").getAsNumber().intValue();
    }

    @Override
    public CommonCompatibilityManager createCompatibilityManager() {
        return new FabricCommonCompatibilityManager();
    }

    @Override
    protected PermissionManager createPermissionManager() {
        return new FabricPermissionManager();
    }

}
