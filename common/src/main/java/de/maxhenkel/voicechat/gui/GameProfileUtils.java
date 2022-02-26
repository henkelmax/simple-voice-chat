package de.maxhenkel.voicechat.gui;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

public class GameProfileUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static ResourceLocation getSkin(UUID uuid) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        PlayerInfo playerInfo = connection.getPlayerInfo(uuid);
        if (playerInfo == null) {
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        return playerInfo.getSkinLocation();
    }

    @Nullable
    public static GameProfileCache getGameProfileCache() {
        Field gameProfileCacheField = getGameProfileCacheField();
        if (gameProfileCacheField == null) {
            return null;
        }
        try {
            return (GameProfileCache) gameProfileCacheField.get(null);
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
        for (Field field : SkullBlockEntity.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!field.getType().equals(GameProfileCache.class)) {
                continue;
            }
            field.setAccessible(true);
            return field;
        }
        return null;
    }

}
