package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.VolumeCategory;
import io.netty.buffer.ByteBuf;

public class AddCategoryPacket implements Packet<AddCategoryPacket> {

    public static final String ADD_CATEGORY = "add_category";

    private VolumeCategory category;

    public AddCategoryPacket() {

    }

    public AddCategoryPacket(VolumeCategory category) {
        this.category = category;
    }

    public VolumeCategory getCategory() {
        return category;
    }

    @Override
    public String getID() {
        return ADD_CATEGORY;
    }

    @Override
    public AddCategoryPacket fromBytes(ByteBuf buf) {
        category = category.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        category.toBytes(buf);
    }

}
