package de.maxhenkel.voicechat.net;

import io.netty.buffer.ByteBuf;

public class RequestSecretPacket implements Packet<RequestSecretPacket> {

    public static final String REQUEST_SECRET = "request_secret";

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
    public String getIdentifier() {
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
