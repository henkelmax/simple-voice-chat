package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.gui.VoiceChatScreen;
import de.maxhenkel.voicechat.gui.VoiceChatSettingsScreen;
import de.maxhenkel.voicechat.gui.group.GroupScreen;
import de.maxhenkel.voicechat.gui.group.JoinGroupScreen;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumesScreen;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class KeyEvents {

    private final Minecraft minecraft;

    public static KeyMapping KEY_PTT;
    public static KeyMapping KEY_WHISPER;
    public static KeyMapping KEY_MUTE;
    public static KeyMapping KEY_DISABLE;
    public static KeyMapping KEY_HIDE_ICONS;
    public static KeyMapping KEY_VOICE_CHAT;
    public static KeyMapping KEY_VOICE_CHAT_SETTINGS;
    public static KeyMapping KEY_GROUP;
    public static KeyMapping KEY_TOGGLE_RECORDING;
    public static KeyMapping KEY_ADJUST_VOLUMES;

    public KeyEvents() {
        minecraft = Minecraft.getInstance();
        ClientCompatibilityManager.INSTANCE.onHandleKeyBinds(this::handleKeybinds);
    }

    public static void registerKeyBinds() {
        if (KEY_PTT != null) {
            throw new IllegalStateException("Registered key binds twice");
        }

        KEY_PTT = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.push_to_talk", GLFW.GLFW_KEY_CAPS_LOCK, "key.categories.voicechat"));
        KEY_WHISPER = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.whisper", InputConstants.UNKNOWN.getValue(), "key.categories.voicechat"));
        KEY_MUTE = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.mute_microphone", GLFW.GLFW_KEY_M, "key.categories.voicechat"));
        KEY_DISABLE = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.disable_voice_chat", GLFW.GLFW_KEY_N, "key.categories.voicechat"));
        KEY_HIDE_ICONS = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.hide_icons", GLFW.GLFW_KEY_H, "key.categories.voicechat"));
        KEY_VOICE_CHAT = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.voice_chat", GLFW.GLFW_KEY_V, "key.categories.voicechat"));
        KEY_VOICE_CHAT_SETTINGS = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.voice_chat_settings", InputConstants.UNKNOWN.getValue(), "key.categories.voicechat"));
        KEY_GROUP = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.voice_chat_group", GLFW.GLFW_KEY_G, "key.categories.voicechat"));
        KEY_TOGGLE_RECORDING = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.voice_chat_toggle_recording", InputConstants.UNKNOWN.getValue(), "key.categories.voicechat"));
        KEY_ADJUST_VOLUMES = ClientCompatibilityManager.INSTANCE.registerKeyBinding(new KeyMapping("key.voice_chat_adjust_volumes", InputConstants.UNKNOWN.getValue(), "key.categories.voicechat"));
    }

    private void handleKeybinds() {
        ClientVoicechat client = ClientManager.getClient();
        ClientPlayerStateManager playerStateManager = ClientManager.getPlayerStateManager();
        if (KEY_VOICE_CHAT.consumeClick()) {
            minecraft.setScreen(new VoiceChatScreen());
        }

        if (KEY_GROUP.consumeClick()) {
            if (client != null && client.getConnection() != null && client.getConnection().getData().groupsEnabled()) {
                if (playerStateManager.isInGroup()) {
                    minecraft.setScreen(new GroupScreen(playerStateManager.getGroup()));
                } else {
                    minecraft.setScreen(new JoinGroupScreen());
                }
            } else {
                minecraft.player.displayClientMessage(Component.translatable("message.voicechat.groups_disabled"), true);
            }
        }

        if (KEY_VOICE_CHAT_SETTINGS.consumeClick()) {
            minecraft.setScreen(new VoiceChatSettingsScreen());
        }

        if (KEY_ADJUST_VOLUMES.consumeClick()) {
            minecraft.setScreen(new AdjustVolumesScreen());
        }

        if (KEY_PTT.consumeClick()) {
            checkConnected();
        }

        if (KEY_WHISPER.consumeClick()) {
            checkConnected();
        }

        if (KEY_MUTE.consumeClick() && checkConnected()) {
            playerStateManager.setMuted(!playerStateManager.isMuted());
        }

        if (KEY_DISABLE.consumeClick() && checkConnected()) {
            playerStateManager.setDisabled(!playerStateManager.isDisabled());
        }

        if (KEY_TOGGLE_RECORDING.consumeClick() && client != null) {
            ClientManager.getClient().toggleRecording();
        }

        if (KEY_HIDE_ICONS.consumeClick()) {
            boolean hidden = !VoicechatClient.CLIENT_CONFIG.hideIcons.get();
            VoicechatClient.CLIENT_CONFIG.hideIcons.set(hidden).save();

            if (hidden) {
                minecraft.player.displayClientMessage(Component.translatable("message.voicechat.icons_hidden"), true);
            } else {
                minecraft.player.displayClientMessage(Component.translatable("message.voicechat.icons_visible"), true);
            }
        }
    }

    private boolean checkConnected() {
        if (ClientManager.getClient() == null || ClientManager.getClient().getConnection() == null || !ClientManager.getClient().getConnection().isInitialized()) {
            sendUnavailableMessage();
            return false;
        }
        return true;
    }

    private void sendUnavailableMessage() {
        minecraft.player.displayClientMessage(Component.translatable("message.voicechat.voice_chat_not_connected"), true);
    }

}