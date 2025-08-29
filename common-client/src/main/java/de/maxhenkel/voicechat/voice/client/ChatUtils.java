package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;

public class ChatUtils {

    public static void sendModErrorMessage(String translationKey, @Nullable String errorMessage) {
        IFormattableTextComponent error = createModMessage(new TranslationTextComponent(translationKey).withStyle(TextFormatting.RED)).withStyle(style -> {
            if (errorMessage != null) {
                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(errorMessage).withStyle(TextFormatting.RED)));
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

    public static void sendModMessage(ITextComponent message) {
        sendPlayerMessage(createModMessage(message));
    }

    public static IFormattableTextComponent createModMessage(ITextComponent message) {
        return new StringTextComponent("")
                .append(TextComponentUtils.wrapInSquareBrackets(new StringTextComponent(CommonCompatibilityManager.INSTANCE.getModName())).withStyle(TextFormatting.GREEN))
                .append(" ")
                .append(message);
    }

    public static void sendPlayerMessage(ITextComponent component) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.sendMessage(component, Util.NIL_UUID);
    }
}
