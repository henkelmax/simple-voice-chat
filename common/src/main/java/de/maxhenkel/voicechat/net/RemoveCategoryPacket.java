package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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
    public RemoveCategoryPacket fromBytes(FriendlyByteBuf buf) {
        categoryId = buf.readUtf(16);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(categoryId, 16);
    }

}
