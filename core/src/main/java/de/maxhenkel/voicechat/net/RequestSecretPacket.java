package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import io.netty.buffer.ByteBuf;
import de.maxhenkel.voicechat.voice.common.ResourceLocation;

public class RequestSecretPacket implements Packet<RequestSecretPacket> {

    public static final ResourceLocation REQUEST_SECRET = new ResourceLocation(Voicechat.MODID, "request_secret");

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
    public RequestSecretPacket fromBytes(ByteBuf buf) {
        compatibilityVersion = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(compatibilityVersion);
    }

}
