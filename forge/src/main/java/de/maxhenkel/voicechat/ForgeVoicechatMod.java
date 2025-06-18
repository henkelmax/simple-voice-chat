package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.config.ConfigMigrator;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeCommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.ForgePermissionManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;

import java.util.Objects;

@Mod(ForgeVoicechatMod.MODID)
public class ForgeVoicechatMod extends Voicechat {

    protected FMLJavaModLoadingContext context;

    public ForgeVoicechatMod(FMLJavaModLoadingContext context) {
        this.context = context;
        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(this::commonSetup);
        if (FMLEnvironment.dist.isClient()) {
            new ForgeVoicechatClientMod(context);
        }
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        initialize();
        LevelEvent.Load.BUS.addListener(ConfigMigrator::onLoadLevel);

        ForgeCommonCompatibilityManager manager = (ForgeCommonCompatibilityManager) CommonCompatibilityManager.INSTANCE;
        ServerStartedEvent.BUS.addListener(manager::serverStarting);
        ServerStoppingEvent.BUS.addListener(manager::serverStopping);
        RegisterCommandsEvent.BUS.addListener(manager::onRegisterCommands);
        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(manager::playerLoggedIn);
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(manager::playerLoggedOut);

        ForgePermissionManager permissionManager = (ForgePermissionManager) PermissionManager.INSTANCE;
        PermissionGatherEvent.Nodes.BUS.addListener(permissionManager::registerPermissions);

        context.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> {
            return new IExtensionPoint.DisplayTest(() -> String.valueOf(Voicechat.COMPATIBILITY_VERSION), (incoming, isNetwork) -> {
                return Objects.equals(incoming, String.valueOf(Voicechat.COMPATIBILITY_VERSION));
            });
        });
    }

}