package de.maxhenkel.voicechat.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class SkinUtils {

    public static ResourceLocation getSkin(UUID uuid) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayNetHandler connection = minecraft.getConnection();
        if (connection == null) {
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        NetworkPlayerInfo playerInfo = connection.getPlayerInfo(uuid);
        if (playerInfo == null) {
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        return playerInfo.getSkinLocation();
    }

}
