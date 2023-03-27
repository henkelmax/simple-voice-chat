package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ForgeVoicechatClientMod extends VoicechatClient {

    public ForgeVoicechatClientMod(FMLPreInitializationEvent event) {
    }

    public void clientSetup(FMLInitializationEvent event) {
        initializeClient();
        MinecraftForge.EVENT_BUS.register(ClientCompatibilityManager.INSTANCE);
    }
}
