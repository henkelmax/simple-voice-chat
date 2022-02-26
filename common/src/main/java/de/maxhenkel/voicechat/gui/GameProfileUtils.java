package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

public class GameProfileUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static ResourceLocation getSkin(UUID uuid) {
        ClientPlayNetHandler connection = mc.getConnection();
        if (connection == null) {
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        NetworkPlayerInfo playerInfo = connection.getPlayerInfo(uuid);
        if (playerInfo == null) {
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        return playerInfo.getSkinLocation();
    }

    @Nullable
    public static PlayerProfileCache getGameProfileCache() {
        Field gameProfileCacheField = getGameProfileCacheField();
        if (gameProfileCacheField == null) {
            return null;
        }
        try {
            return (PlayerProfileCache) gameProfileCacheField.get(null);
        } catch (IllegalAccessException e) {
            Voicechat.LOGGER.warn("Failed to access GameProfileCache: {}", e.getMessage());
            return null;
        }
    }

    private static Field gameProfileCache;

    @Nullable
    private static Field getGameProfileCacheField() {
        if (gameProfileCache == null) {
            gameProfileCache = findGameProfileCacheField();
            if (gameProfileCache == null) {
                Voicechat.LOGGER.warn("Failed to find GameProfileCache field");
            }
        }
        return gameProfileCache;
    }

    @Nullable
    private static Field findGameProfileCacheField() {
        for (Field field : SkullTileEntity.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!field.getType().equals(PlayerProfileCache.class)) {
                continue;
            }
            field.setAccessible(true);
            return field;
        }
        return null;
    }

}
