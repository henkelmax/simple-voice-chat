package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.config.ForgeClientConfig;
import de.maxhenkel.voicechat.config.ForgeServerConfig;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.ForgeCommonCompatibilityManager;
import de.maxhenkel.voicechat.permission.ForgePermissionManager;
import de.maxhenkel.voicechat.permission.PermissionManager;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

@Mod(ForgeVoicechatMod.MODID)
public class ForgeVoicechatMod extends Voicechat {

    private final ForgeCommonCompatibilityManager compatibilityManager;

    public ForgeVoicechatMod() {
        compatibilityManager = new ForgeCommonCompatibilityManager();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        SERVER_CONFIG = registerConfig(ModConfig.Type.SERVER, ForgeServerConfig::new);
        VoicechatClient.CLIENT_CONFIG = ForgeVoicechatMod.registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig::new);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        initialize();
        MinecraftForge.EVENT_BUS.register(compatibilityManager);
    }

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
    protected CommonCompatibilityManager createCompatibilityManager() {
        return compatibilityManager;
    }

    @Override
    protected PermissionManager createPermissionManager() {
        ForgePermissionManager permissionManager = new ForgePermissionManager();
        MinecraftForge.EVENT_BUS.register(permissionManager);
        return permissionManager;
    }

    public static <T> T registerConfig(ModConfig.Type type, Function<ForgeConfigSpec.Builder, T> consumer) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        T config = consumer.apply(builder);
        ForgeConfigSpec spec = builder.build();
        ModLoadingContext.get().registerConfig(type, spec);
        return config;
    }
}