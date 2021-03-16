package de.maxhenkel.voicechat;

import de.maxhenkel.voicechat.config.ClientConfig;
import de.maxhenkel.voicechat.config.ConfigBuilder;
import de.maxhenkel.voicechat.config.PlayerVolumeConfig;
import de.maxhenkel.voicechat.net.Packets;
import de.maxhenkel.voicechat.voice.client.ClientVoiceEvents;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class VoicechatClient implements ClientModInitializer {

    public static KeyBinding KEY_PTT;
    public static KeyBinding KEY_MUTE;
    public static KeyBinding KEY_DISABLE;
    public static KeyBinding KEY_HIDE_ICONS;
    public static KeyBinding KEY_VOICE_CHAT_SETTINGS;

    public static ClientVoiceEvents CLIENT;
    public static ClientConfig CLIENT_CONFIG;
    public static PlayerVolumeConfig VOLUME_CONFIG;

    @Override
    public void onInitializeClient() {
        ClientLoginNetworking.registerGlobalReceiver(Packets.INIT, (client, handler, buf, listenerAdder) -> {
            int serverCompatibilityVersion = buf.readInt();

            if (serverCompatibilityVersion != Voicechat.COMPATIBILITY_VERSION) {
                Voicechat.LOGGER.warn("Incompatible voice chat version (server={}, client={})", serverCompatibilityVersion, Voicechat.COMPATIBILITY_VERSION);
            }

            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            buffer.writeInt(Voicechat.COMPATIBILITY_VERSION);
            return CompletableFuture.completedFuture(buffer);
        });

        ConfigBuilder.create(MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-client.properties"), builder -> CLIENT_CONFIG = new ClientConfig(builder));
        VOLUME_CONFIG = new PlayerVolumeConfig(MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("voicechat-volumes.properties"));

        KEY_PTT = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.push_to_talk", GLFW.GLFW_KEY_CAPS_LOCK, "key.categories.voicechat"));
        KEY_MUTE = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mute_microphone", GLFW.GLFW_KEY_M, "key.categories.voicechat"));
        KEY_DISABLE = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.disable_voice_chat", GLFW.GLFW_KEY_N, "key.categories.voicechat"));
        KEY_HIDE_ICONS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.hide_icons", GLFW.GLFW_KEY_H, "key.categories.voicechat"));
        KEY_VOICE_CHAT_SETTINGS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voice_chat_settings", GLFW.GLFW_KEY_V, "key.categories.voicechat"));

        CLIENT = new ClientVoiceEvents();
    }
}
