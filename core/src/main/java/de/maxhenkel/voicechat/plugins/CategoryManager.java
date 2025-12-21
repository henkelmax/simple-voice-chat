package de.maxhenkel.voicechat.plugins;

import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryManager {

    protected final Map<String, VolumeCategoryImpl> categories;

    public CategoryManager() {
        categories = new ConcurrentHashMap<>();
    }

    public void addCategory(VolumeCategoryImpl category) {
        categories.put(category.getId(), category);
    }

    @Nullable
    public VolumeCategoryImpl removeCategory(String categoryId) {
        return categories.remove(categoryId);
    }

    public Collection<VolumeCategoryImpl> getCategories() {
        return categories.values();
    }
}
