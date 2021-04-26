package de.maxhenkel.voicechat;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.config.ClientConfig;
import de.maxhenkel.voicechat.config.ConfigBuilder;
import de.maxhenkel.voicechat.config.PlayerVolumeConfig;
import de.maxhenkel.voicechat.voice.client.ClientVoiceEvents;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class VoicechatClient implements ClientModInitializer {

    public static KeyMapping KEY_PTT;
    public static KeyMapping KEY_MUTE;
    public static KeyMapping KEY_DISABLE;
    public static KeyMapping KEY_HIDE_ICONS;
    public static KeyMapping KEY_VOICE_CHAT;
    public static KeyMapping KEY_VOICE_CHAT_SETTINGS;
    public static KeyMapping KEY_GROUP;

    public static ClientVoiceEvents CLIENT;
    public static ClientConfig CLIENT_CONFIG;
    public static PlayerVolumeConfig VOLUME_CONFIG;

    @Override
    public void onInitializeClient() {
        ClientLoginNetworking.registerGlobalReceiver(Voicechat.INIT, (client, handler, buf, listenerAdder) -> {
            int serverCompatibilityVersion = buf.readInt();

            if (serverCompatibilityVersion != Voicechat.COMPATIBILITY_VERSION) {
                Voicechat.LOGGER.warn("Incompatible voice chat version (server={}, client={})", serverCompatibilityVersion, Voicechat.COMPATIBILITY_VERSION);
            }

            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeInt(Voicechat.COMPATIBILITY_VERSION);
            return CompletableFuture.completedFuture(buffer);
        });

        ConfigBuilder.create(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Voicechat.MODID).resolve("voicechat-client.properties"), builder -> CLIENT_CONFIG = new ClientConfig(builder));
        VOLUME_CONFIG = new PlayerVolumeConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("voicechat-volumes.properties"));

        KEY_PTT = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.push_to_talk", GLFW.GLFW_KEY_CAPS_LOCK, "key.categories.voicechat"));
        KEY_MUTE = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.mute_microphone", GLFW.GLFW_KEY_M, "key.categories.voicechat"));
        KEY_DISABLE = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.disable_voice_chat", GLFW.GLFW_KEY_N, "key.categories.voicechat"));
        KEY_HIDE_ICONS = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.hide_icons", GLFW.GLFW_KEY_H, "key.categories.voicechat"));
        KEY_VOICE_CHAT = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.voice_chat", GLFW.GLFW_KEY_V, "key.categories.voicechat"));
        KEY_VOICE_CHAT_SETTINGS = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.voice_chat_settings", InputConstants.UNKNOWN.getValue(), "key.categories.voicechat"));
        KEY_GROUP = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.voice_chat_group", InputConstants.UNKNOWN.getValue(), "key.categories.voicechat"));

        CLIENT = new ClientVoiceEvents();
    }
}
