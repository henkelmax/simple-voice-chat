package de.maxhenkel.voicechat.voice.client;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.gui.volume.AdjustVolumeList;
import de.maxhenkel.voicechat.intercompatibility.ClientCompatibilityManager;
import de.maxhenkel.voicechat.intercompatibility.CommonCompatibilityManager;
import de.maxhenkel.voicechat.plugins.CategoryManager;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientCategoryManager extends CategoryManager {

    protected final Map<String, CustomTextureObject> images;

    public ClientCategoryManager() {
        images = new ConcurrentHashMap<>();
        CommonCompatibilityManager.INSTANCE.getNetManager().addCategoryChannel.setClientListener((client, handler, packet) -> {
            addCategory(packet.getCategory());
            Voicechat.LOGGER.debug("Added category {}", packet.getCategory().getId());
        });
        CommonCompatibilityManager.INSTANCE.getNetManager().removeCategoryChannel.setClientListener((client, handler, packet) -> {
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

    private void registerImage(String id, BufferedImage image) {
        ResourceLocation resourceLocation = new ResourceLocation(Voicechat.MODID, "category_" + id);//Minecraft.getMinecraft().getEntityRenderDispatcher().textureManager.register(id, new CustomTextureObject(image));
        images.put(id, new CustomTextureObject(resourceLocation, image));
    }

    private void unRegisterImage(String id) {
        CustomTextureObject customTextureObject = images.get(id);
        if (customTextureObject != null) {
            customTextureObject.deleteGlTexture();
            images.remove(id);
        }
    }

    private BufferedImage fromIntArray(int[][] icon) {
        if (icon.length != 16) {
            throw new IllegalStateException("Icon is not 16x16");
        }
        BufferedImage nativeImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < icon.length; x++) {
            if (icon[x].length != 16) {
                throw new IllegalStateException("Icon is not 16x16");
            }
            for (int y = 0; y < icon.length; y++) {
                nativeImage.setRGB(x, y, icon[x][y]);
            }
        }
        return nativeImage;
    }

    public ResourceLocation getTexture(String id, ResourceLocation defaultImage) {
        CustomTextureObject customTextureObject = images.get(id);
        if (customTextureObject == null) {
            return defaultImage;
        }
        return customTextureObject.getResourceLocation();
    }

    private static class CustomTextureObject extends SimpleTexture {

        private final BufferedImage image;
        private final ResourceLocation resourceLocation;

        public CustomTextureObject(ResourceLocation textureResourceLocation, BufferedImage image) {
            super(textureResourceLocation);
            this.resourceLocation = textureResourceLocation;
            this.image = image;
        }

        @Override
        public void loadTexture(IResourceManager resourceManager) throws IOException {
            TextureUtil.uploadTextureImage(super.getGlTextureId(), image);
        }

        public ResourceLocation getResourceLocation() {
            return resourceLocation;
        }
    }

}
