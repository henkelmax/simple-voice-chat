package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.voice.common.BufferUtils;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

public class RemoveCategoryPacket implements Packet<RemoveCategoryPacket> {

    public static final ResourceLocation REMOVE_CATEGORY = new ResourceLocation(Voicechat.MODID, "remove_category");

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
    public ResourceLocation getIdentifier() {
        return REMOVE_CATEGORY;
    }

    @Override
    public RemoveCategoryPacket fromBytes(ByteBuf buf) {
        categoryId = BufferUtils.readUtf(buf, 16);
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        BufferUtils.writeUtf(buf, categoryId, 16);
    }

}
