package de.maxhenkel.voicechat;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.corelib.CommonRegistry;
import de.maxhenkel.voicechat.command.VoicechatCommands;
import de.maxhenkel.voicechat.net.*;
import de.maxhenkel.voicechat.resourcepacks.VoiceChatResourcePack;
import de.maxhenkel.voicechat.voice.client.ClientVoiceEvents;
import de.maxhenkel.voicechat.voice.server.ServerVoiceEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import net.minecraftforge.fmlserverevents.FMLServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.regex.Pattern;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "voicechat";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static SimpleChannel SIMPLE_CHANNEL;

    public static int PROTOCOL_VERSION = 3;

    public static ServerConfig SERVER_CONFIG;
    public static ClientConfig CLIENT_CONFIG;
    public static PlayerVolumeConfig VOLUME_CONFIG;

    public static ServerVoiceEvents SERVER;
    @OnlyIn(Dist.CLIENT)
    public static ClientVoiceEvents CLIENT_VOICE_EVENTS;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_PTT;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_MUTE;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_DISABLE;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_VOICE_CHAT;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_HIDE_ICONS;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_VOICE_CHAT_SETTINGS;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_GROUP;

    @OnlyIn(Dist.CLIENT)
    public static KeyMapping KEY_TOGGLE_RECORDING;

    public static final Pattern GROUP_REGEX = Pattern.compile("^\\S[^\"\\n\\r\\t]{0,15}$");

    public static VoiceChatResourcePack CLASSIC_ICONS;
    public static VoiceChatResourcePack WHITE_ICONS;
    public static VoiceChatResourcePack BLACK_ICONS;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class, true);
        CLIENT_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.CLIENT, ClientConfig.class);
        VOLUME_CONFIG = new PlayerVolumeConfig();
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        SERVER = new ServerVoiceEvents();
        MinecraftForge.EVENT_BUS.register(SERVER);

        SIMPLE_CHANNEL = CommonRegistry.registerChannel(Main.MODID, "default", PROTOCOL_VERSION);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 0, AuthenticationMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 1, PlayerStateMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 2, PlayerStatesMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 3, SetPlayerStateMessage.class);
        CommonRegistry.registerMessage(SIMPLE_CHANNEL, 4, SetGroupMessage.class);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        CLIENT_VOICE_EVENTS = new ClientVoiceEvents();
        MinecraftForge.EVENT_BUS.register(CLIENT_VOICE_EVENTS);

        KEY_PTT = new KeyMapping("key.push_to_talk", GLFW.GLFW_KEY_CAPS_LOCK, "key.categories.voicechat");
        ClientRegistry.registerKeyBinding(KEY_PTT);

        KEY_MUTE = new KeyMapping("key.mute_microphone", GLFW.GLFW_KEY_M, "key.categories.voicechat");
        ClientRegistry.registerKeyBinding(KEY_MUTE);

        KEY_DISABLE = new KeyMapping("key.disable_voice_chat", GLFW.GLFW_KEY_N, "key.categories.voicechat");
        ClientRegistry.registerKeyBinding(KEY_DISABLE);

        KEY_HIDE_ICONS = new KeyMapping("key.hide_icons", GLFW.GLFW_KEY_H, "key.categories.voicechat");
        ClientRegistry.registerKeyBinding(KEY_HIDE_ICONS);

        KEY_VOICE_CHAT = new KeyMapping("key.voice_chat", GLFW.GLFW_KEY_V, "key.categories.voicechat");
        ClientRegistry.registerKeyBinding(KEY_VOICE_CHAT);

        KEY_VOICE_CHAT_SETTINGS = new KeyMapping("key.voice_chat_settings", InputConstants.UNKNOWN.getValue(), "key.categories.voicechat");
        ClientRegistry.registerKeyBinding(KEY_VOICE_CHAT_SETTINGS);

        KEY_GROUP = new KeyMapping("key.voice_chat_group", GLFW.GLFW_KEY_G, "key.categories.voicechat");
        ClientRegistry.registerKeyBinding(KEY_GROUP);

        KEY_TOGGLE_RECORDING = new KeyMapping("key.voice_chat_toggle_recording", InputConstants.UNKNOWN.getValue(), "key.categories.voicechat");
        ClientRegistry.registerKeyBinding(KEY_TOGGLE_RECORDING);

        CLASSIC_ICONS = new VoiceChatResourcePack("Classic Icons", "classic_icons");
        WHITE_ICONS = new VoiceChatResourcePack("White Icons", "white_icons");
        BLACK_ICONS = new VoiceChatResourcePack("Black Icons", "black_icons");

        PackRepository repository = Minecraft.getInstance().getResourcePackRepository();
        repository.addPackFinder((Consumer<Pack> consumer, Pack.PackConstructor packConstructor) -> {
                    consumer.accept(Pack.create(CLASSIC_ICONS.getName(), false, () -> CLASSIC_ICONS, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN));
                    consumer.accept(Pack.create(WHITE_ICONS.getName(), false, () -> WHITE_ICONS, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN));
                    consumer.accept(Pack.create(BLACK_ICONS.getName(), false, () -> BLACK_ICONS, packConstructor, Pack.Position.TOP, PackSource.BUILT_IN));
                }
        );
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartedEvent event) {
        SERVER.serverStarting(event);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        VoicechatCommands.register(event.getDispatcher());
    }

}
