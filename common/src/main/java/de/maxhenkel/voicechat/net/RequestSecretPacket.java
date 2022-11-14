package de.maxhenkel.voicechat.net;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class RequestSecretPacket implements Packet<RequestSecretPacket> {

    public static final ResourceLocation REQUEST_SECRET = new ResourceLocation(NetManager.CHANNEL, "request_secret");

    private int compatibilityVersion;

    public RequestSecretPacket() {

    }

    public RequestSecretPacket(int compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    public int getCompatibilityVersion() {
        return compatibilityVersion;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return REQUEST_SECRET;
    }

    @Override
    public RequestSecretPacket fromBytes(PacketBuffer buf) {
        compatibilityVersion = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeInt(compatibilityVersion);
    }

}
