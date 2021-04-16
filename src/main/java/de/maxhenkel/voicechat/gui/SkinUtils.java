package de.maxhenkel.voicechat.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

import java.util.Map;

public class SkinUtils {

    public static Identifier getSkin(GameProfile gameProfile) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinProvider().getTextures(gameProfile);

        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            return minecraft.getSkinProvider().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        } else {
            return DefaultSkinHelper.getTexture(gameProfile.getId());
        }
    }

}
