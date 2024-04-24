package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.NeoForgeCommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(NeoForgeVoicechatMod.MODID)
public class NeoForgeVoicechatMod extends Voicechat {

    private IEventBus modEventBus;

    public NeoForgeVoicechatMod(IEventBus eventBus) {
        modEventBus = eventBus;
        modEventBus.addListener(this::commonSetup);
        if (FMLEnvironment.dist.isClient()) {
            new NeoForgeVoicechatClientMod(eventBus);
        }
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        initialize();
        modEventBus.register(NeoForgeCommonCompatibilityManager.INSTANCE.getNetManager());
        NeoForge.EVENT_BUS.register(CommonCompatibilityManager.INSTANCE);
        NeoForge.EVENT_BUS.register(PermissionManager.INSTANCE);
    }

}