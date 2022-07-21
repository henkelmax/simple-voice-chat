package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class AddCategoryPacket implements Packet<AddCategoryPacket> {

    public static final ResourceLocation ADD_CATEGORY = new ResourceLocation(Voicechat.MODID, "add_category");

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
    public ResourceLocation getIdentifier() {
        return ADD_CATEGORY;
    }

    @Override
    public AddCategoryPacket fromBytes(PacketBuffer buf) {
        category = VolumeCategoryImpl.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        category.toBytes(buf);
    }

}
