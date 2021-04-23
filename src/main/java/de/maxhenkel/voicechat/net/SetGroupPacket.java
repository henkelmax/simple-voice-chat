package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SetGroupPacket implements Packet<SetGroupPacket> {

    public static final Identifier SET_GROUP = new Identifier(Voicechat.MODID, "set_group");

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
    public Identifier getID() {
        return SET_GROUP;
    }

    @Override
    public SetGroupPacket fromBytes(PacketByteBuf buf) {
        group = buf.readString(16);
        return this;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeString(group, 16);
    }

}
