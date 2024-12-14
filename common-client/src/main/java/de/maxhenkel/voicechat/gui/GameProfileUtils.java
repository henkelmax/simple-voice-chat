package de.maxhenkel.voicechat.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;

import java.util.UUID;

public class GameProfileUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static PlayerSkin getSkin(UUID uuid) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return DefaultPlayerSkin.get(uuid);
        }
        PlayerInfo playerInfo = connection.getPlayerInfo(uuid);
        if (playerInfo == null) {
            return DefaultPlayerSkin.get(uuid);
        }
        return playerInfo.getSkin();
    }

}
