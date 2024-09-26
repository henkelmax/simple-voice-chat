package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public class ChatUtils {

    public static void sendPlayerError(String translationKey, @Nullable Exception e) {
        MutableComponent error = createModMessage(Component.translatable(translationKey).withStyle(ChatFormatting.RED)).withStyle(style -> {
            if (e != null) {
                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(e.getMessage()).withStyle(ChatFormatting.RED)));
            }
            return style;
        });
        sendPlayerMessage(error);
    }

    public static void sendModMessage(Component message) {
        sendPlayerMessage(createModMessage(message));
    }

    public static MutableComponent createModMessage(Component message) {
        return Component.empty()
                .append(ComponentUtils.wrapInSquareBrackets(Component.literal(CommonCompatibilityManager.INSTANCE.getModName())).withStyle(ChatFormatting.GREEN))
                .append(" ")
                .append(message);
    }

    public static void sendPlayerMessage(Component component) {
        Minecraft.getInstance().gui.getChat().addMessage(component);
    }

}
