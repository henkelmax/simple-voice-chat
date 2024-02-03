package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;

public class ChatUtils {

    public static void sendPlayerError(String translationKey, @Nullable Exception e) {
        Style style = new Style().setColor(TextFormatting.RED);
        if (e != null) {
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(e.getMessage()).setStyle(new Style().setColor(TextFormatting.RED))));
        }
        ITextComponent message = wrapInSquareBrackets(new TextComponentString(CommonCompatibilityManager.INSTANCE.getModName()))
                .setStyle(new Style().setColor(TextFormatting.GREEN))
                .appendText(" ")
                .appendSibling(new TextComponentTranslation(translationKey).setStyle(style));
        sendPlayerMessage(message);
    }

    private static ITextComponent wrapInSquareBrackets(ITextComponent component) {
        return new TextComponentString("[").appendSibling(component).appendText("]");
    }

    public static void sendModMessage(ITextComponent message) {
        sendPlayerMessage(createModMessage(message));
    }

    public static ITextComponent createModMessage(ITextComponent message) {
        return new TextComponentString("")
                .appendSibling(wrapInSquareBrackets(new TextComponentString(CommonCompatibilityManager.INSTANCE.getModName())).setStyle(new Style().setColor(TextFormatting.GREEN)))
                .appendText(" ")
                .appendSibling(message);
    }

    public static void sendPlayerMessage(ITextComponent component) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) {
            return;
        }
        player.sendMessage(component);
    }
}
