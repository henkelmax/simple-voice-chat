package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.*;

import javax.annotation.Nullable;

public class ChatUtils {

    public static void sendPlayerError(String translationKey, @Nullable Exception e) {
        MutableComponent error = createModMessage(new TranslatableComponent(translationKey).withStyle(ChatFormatting.RED)).withStyle(style -> {
            if (e != null) {
                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(e.getMessage()).withStyle(ChatFormatting.RED)));
            }
            return style;
        });
        sendPlayerMessage(error);
    }

    public static void sendModMessage(Component message) {
        sendPlayerMessage(createModMessage(message));
    }

    public static MutableComponent createModMessage(Component message) {
        return new TextComponent("")
                .append(ComponentUtils.wrapInSquareBrackets(new TextComponent(CommonCompatibilityManager.INSTANCE.getModName())).withStyle(ChatFormatting.GREEN))
                .append(" ")
                .append(message);
    }

    public static void sendPlayerMessage(Component component) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.sendMessage(component, Util.NIL_UUID);
    }
}
