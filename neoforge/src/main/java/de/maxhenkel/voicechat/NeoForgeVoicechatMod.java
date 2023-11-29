package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.PermissionManager;

import java.util.Objects;

import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NeoForgeVoicechatMod.MODID)
public class NeoForgeVoicechatMod extends Voicechat {

    public NeoForgeVoicechatMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        if (FMLEnvironment.dist.isClient()) {
            new NeoForgeVoicechatClientMod();
        }
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        initialize();
        NeoForge.EVENT_BUS.register(CommonCompatibilityManager.INSTANCE);
        NeoForge.EVENT_BUS.register(PermissionManager.INSTANCE);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> {
            return new IExtensionPoint.DisplayTest(() -> String.valueOf(Voicechat.COMPATIBILITY_VERSION), (incoming, isNetwork) -> {
                return Objects.equals(incoming, String.valueOf(Voicechat.COMPATIBILITY_VERSION));
            });
        });
    }

}