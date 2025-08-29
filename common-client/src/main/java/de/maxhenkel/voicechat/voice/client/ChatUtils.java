package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public class ChatUtils {

    public static void sendModErrorMessage(String translationKey, @Nullable String errorMessage) {
        MutableComponent error = createModMessage(Component.translatable(translationKey).withStyle(ChatFormatting.RED)).withStyle(style -> {
            if (errorMessage != null) {
                return style.withHoverEvent(new HoverEvent.ShowText(Component.literal(errorMessage).withStyle(ChatFormatting.RED)));
            }
            return style;
        });
        sendPlayerMessage(error);
    }

    public static void sendModErrorMessage(String translationKey, @Nullable Exception e) {
        sendModErrorMessage(translationKey, e == null ? null : e.getMessage());
    }

    public static void sendModErrorMessage(String translationKey) {
        sendModErrorMessage(translationKey, (String) null);
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
