package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import org.bukkit.NamespacedKey;

public class RemoveCategoryPacket implements Packet<RemoveCategoryPacket> {

    public static final NamespacedKey REMOVE_CATEGORY = Voicechat.compatibility.createNamespacedKey("remove_category");

    private String categoryId;

    public RemoveCategoryPacket() {

    }

    public RemoveCategoryPacket(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    @Override
    public NamespacedKey getID() {
        return REMOVE_CATEGORY;
    }

    @Override
    public RemoveCategoryPacket fromBytes(FriendlyByteBuf buf) {
        categoryId = buf.readUtf(16);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(categoryId, 16);
    }

}
