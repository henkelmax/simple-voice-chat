package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SetGroupPacket implements Packet<SetGroupPacket> {

    public static final ResourceLocation SET_GROUP = new ResourceLocation(Voicechat.MODID, "set_group");

    private String group;

    public SetGroupPacket() {

    }

    public SetGroupPacket(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return SET_GROUP;
    }

    @Override
    public int getID() {
        return 5;
    }

    @Override
    public SetGroupPacket fromBytes(FriendlyByteBuf buf) {
        group = buf.readUtf(512);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(group, 512);
    }

}
