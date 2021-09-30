package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeCommonCompatibilityManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.InputStream;
import java.util.Properties;

@Mod(ForgeVoicechatMod.MODID)
public class ForgeVoicechatMod extends Voicechat {

    private final ForgeCommonCompatibilityManager compatibilityManager;

    public ForgeVoicechatMod() {
        compatibilityManager = new ForgeCommonCompatibilityManager();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        initialize();
        MinecraftForge.EVENT_BUS.register(compatibilityManager);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        new ForgeVoicechatClientMod();
    }

    @Override
    public int readCompatibilityVersion() throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream("compatibility.properties");
        Properties props = new Properties();
        props.load(in);
        return Integer.parseInt(props.getProperty("compatibility_version"));
    }

    @Override
    public CommonCompatibilityManager createCompatibilityManager() {
        return compatibilityManager;
    }
}