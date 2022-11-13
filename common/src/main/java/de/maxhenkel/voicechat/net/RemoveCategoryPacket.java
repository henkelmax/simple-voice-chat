package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

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
    public RemoveCategoryPacket fromBytes(PacketBuffer buf) {
        categoryId = buf.readString(16);
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeString(categoryId);
    }

}
