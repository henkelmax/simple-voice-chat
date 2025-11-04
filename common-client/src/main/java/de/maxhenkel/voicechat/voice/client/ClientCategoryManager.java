package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.platform.NativeImage;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeList;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.ClientServerNetManager;
import de.maxhenkel.voicechat.plugins.CategoryManager;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientCategoryManager extends CategoryManager {

    protected final Map<String, Identifier> images;

    public ClientCategoryManager() {
        images = new ConcurrentHashMap<>();
        ClientServerNetManager.setClientListener(CommonCompatibilityManager.INSTANCE.getNetManager().addCategoryChannel, (player, packet) -> {
            addCategory(packet.getCategory());
            Voicechat.LOGGER.debug("Added category {}", packet.getCategory().getId());
        });
        ClientServerNetManager.setClientListener(CommonCompatibilityManager.INSTANCE.getNetManager().removeCategoryChannel, (player, packet) -> {
            removeCategory(packet.getCategoryId());
            Voicechat.LOGGER.debug("Removed category {}", packet.getCategoryId());
        });
        ClientCompatibilityManager.INSTANCE.onDisconnect(this::clear);
    }

    @Override
    public void addCategory(VolumeCategoryImpl category) {
        super.addCategory(category);

        if (category.getIcon() != null) {
            registerImage(category.getId(), fromIntArray(category.getIcon()));
        }
        AdjustVolumeList.update();
    }

    @Override
    @Nullable
    public VolumeCategoryImpl removeCategory(String categoryId) {
        VolumeCategoryImpl volumeCategory = super.removeCategory(categoryId);
        unRegisterImage(categoryId);
        AdjustVolumeList.update();
        return volumeCategory;
    }

    public void clear() {
        categories.keySet().forEach(this::unRegisterImage);
        categories.clear();
    }

    private void registerImage(String id, NativeImage image) {
        Identifier identifier = Identifier.fromNamespaceAndPath(Voicechat.MODID, id);
        Minecraft.getInstance().getEntityRenderDispatcher().textureManager.register(identifier, new DynamicTexture(identifier::toString, image));
        images.put(id, identifier);
    }

    private void unRegisterImage(String id) {
        Identifier identifier = images.get(id);
        if (identifier != null) {
            Minecraft.getInstance().getEntityRenderDispatcher().textureManager.release(identifier);
            images.remove(id);
        }
    }

    private NativeImage fromIntArray(int[][] icon) {
        if (icon.length != 16) {
            throw new IllegalStateException("Icon is not 16x16");
        }
        NativeImage nativeImage = new NativeImage(16, 16, true);
        for (int x = 0; x < icon.length; x++) {
            if (icon[x].length != 16) {
                nativeImage.close();
                throw new IllegalStateException("Icon is not 16x16");
            }
            for (int y = 0; y < icon.length; y++) {
                nativeImage.setPixel(x, y, icon[x][y]);
            }
        }
        return nativeImage;
    }

    public Identifier getTexture(String id, Identifier defaultImage) {
        return images.getOrDefault(id, defaultImage);
    }

}
