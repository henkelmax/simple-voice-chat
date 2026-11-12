package de.maxhenkel.voicechat.voice.client;

import com.mojang.blaze3d.platform.NativeImage;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.config.CategoryVolumeConfig;
import de.maxhenkel.voicechat.config.VolumeConfigBase;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeList;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.net.ClientServerNetManager;
import de.maxhenkel.voicechat.plugins.CategoryManager;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientCategoryManager extends CategoryManager {

    protected final Map<String, ResourceLocation> images;

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

    @Nullable
    public VolumeCategoryImpl getParent(String categoryId) {
        return getParent(categoryId, categories);
    }

    @Nullable
    public static VolumeCategoryImpl getParent(String categoryId, Map<String, VolumeCategoryImpl> categories) {
        VolumeCategoryImpl category = categories.get(categoryId);
        if (category == null || category.getParent() == null) {
            return null;
        }
        VolumeCategoryImpl parent = categories.get(category.getParent());
        if (parent == null || parent.getParent() != null) {
            return null;
        }
        return parent;
    }

    public double getEffectiveVolume(String categoryId) {
        double volume = getVolume(categoryId);
        VolumeCategoryImpl parent = getParent(categoryId);
        if (parent == null) {
            return volume;
        }
        return Math.min(volume * getVolume(parent.getId()), VolumeConfigBase.MAX_VOLUME);
    }

    private static double getVolume(String categoryId) {
        CategoryVolumeConfig config = VoicechatClient.CATEGORY_VOLUME_CONFIG;
        if (config == null) {
            return 1D;
        }
        return config.getVolume(categoryId);
    }

    public void clear() {
        categories.keySet().forEach(this::unRegisterImage);
        categories.clear();
    }

    private void registerImage(String id, NativeImage image) {
        ResourceLocation resourceLocation = Minecraft.getInstance().getEntityRenderDispatcher().textureManager.register(id, new DynamicTexture(image));
        images.put(id, resourceLocation);
    }

    private void unRegisterImage(String id) {
        ResourceLocation resourceLocation = images.get(id);
        if (resourceLocation != null) {
            Minecraft.getInstance().getEntityRenderDispatcher().textureManager.release(resourceLocation);
            images.remove(id);
        }
    }

    private NativeImage fromIntArray(int[][] icon) {
        NativeImage nativeImage = new NativeImage(16, 16, true);
        for (int x = 0; x < icon.length; x++) {
            for (int y = 0; y < icon.length; y++) {
                nativeImage.setPixelRGBA(x, y, icon[x][y]);
            }
        }
        return nativeImage;
    }

    public ResourceLocation getTexture(String id, ResourceLocation defaultImage) {
        return images.getOrDefault(id, defaultImage);
    }

}
