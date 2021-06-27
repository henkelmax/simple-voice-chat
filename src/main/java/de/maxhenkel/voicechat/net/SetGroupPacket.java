package de.maxhenkel.voicechat.net;

import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;

public class SetGroupPacket implements Packet<SetGroupPacket> {

    public static final MinecraftKey SET_GROUP = new MinecraftKey(Voicechat.MODID, "set_group");

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
    public MinecraftKey getID() {
        return SET_GROUP;
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
