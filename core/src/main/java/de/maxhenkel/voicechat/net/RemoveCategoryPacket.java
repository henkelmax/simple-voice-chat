package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.VCByteBuf;

public class RemoveCategoryPacket implements Packet<RemoveCategoryPacket> {

    public static final String REMOVE_CATEGORY = "remove_category";

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
    public String getID() {
        return REMOVE_CATEGORY;
    }

    @Override
    public RemoveCategoryPacket fromBytes(VCByteBuf buf) {
        categoryId = buf.readUtf(16);
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {
        buf.writeUtf(categoryId, 16);
    }

}
