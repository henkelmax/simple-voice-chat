package de.maxhenkel.voicechat.plugins;

import de.maxhenkel.voicechat.api.VolumeCategory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryManager {

    protected final Map<String, VolumeCategory> categories;

    public CategoryManager() {
        categories = new ConcurrentHashMap<>();
    }

    public void addCategory(VolumeCategory category) {
        categories.put(category.getId(), category);
    }

    @Nullable
    public VolumeCategory removeCategory(String categoryId) {
        return categories.remove(categoryId);
    }

    public Collection<VolumeCategory> getCategories() {
        return categories.values();
    }
}
