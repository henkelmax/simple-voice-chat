package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.NamespacedKeyUtil;
import org.bukkit.NamespacedKey;

public class AddCategoryPacket implements Packet<AddCategoryPacket> {

    public static final NamespacedKey ADD_CATEGORY = NamespacedKeyUtil.voicechat("add_category");

    private VolumeCategoryImpl category;

    public AddCategoryPacket() {

    }

    public AddCategoryPacket(VolumeCategoryImpl category) {
        this.category = category;
    }

    public VolumeCategoryImpl getCategory() {
        return category;
    }

    @Override
    public NamespacedKey getID() {
        return ADD_CATEGORY;
    }

    @Override
    public AddCategoryPacket fromBytes(FriendlyByteBuf buf) {
        category = VolumeCategoryImpl.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        category.toBytes(buf);
    }

}
