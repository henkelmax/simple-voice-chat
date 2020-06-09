package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.net.AuthenticationMessage;
import de.maxhenkel.voicechat.voice.client.ClientVoiceEvents;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "voicechat";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static SimpleChannel SIMPLE_CHANNEL;

    public static ServerVoiceEvents SERVER_VOICE_EVENTS;
    @OnlyIn(Dist.CLIENT)
    public static ClientVoiceEvents CLIENT_VOICE_EVENTS;

    @OnlyIn(Dist.CLIENT)
    public static KeyBinding KEY_PTT;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverStarting);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);

    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        SERVER_VOICE_EVENTS = new ServerVoiceEvents();
        MinecraftForge.EVENT_BUS.register(SERVER_VOICE_EVENTS);

        SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Main.MODID, "default"), () -> "1.0.0", s -> true, s -> true);
        SIMPLE_CHANNEL.registerMessage(0, AuthenticationMessage.class, (msg, buf) -> msg.toBytes(buf), (buf) -> new AuthenticationMessage().fromBytes(buf), (msg, fun) -> msg.executeClientSide(fun.get()));
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        CLIENT_VOICE_EVENTS = new ClientVoiceEvents();
        MinecraftForge.EVENT_BUS.register(CLIENT_VOICE_EVENTS);

        KEY_PTT = new KeyBinding("key.push_to_talk", GLFW.GLFW_KEY_CAPS_LOCK, "key.categories.misc");
        ClientRegistry.registerKeyBinding(KEY_PTT);

        //ScreenManager.IScreenFactory factory = (ScreenManager.IScreenFactory<ContainerAlbumInventory, AlbumInventoryScreen>) (container, playerInventory, name) -> new AlbumInventoryScreen(playerInventory, container, name);
        //ScreenManager.registerFactory(Main.ALBUM_INVENTORY_CONTAINER, factory);
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartedEvent event) {
        SERVER_VOICE_EVENTS.serverStarting(event);
    }

}
