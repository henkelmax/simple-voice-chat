package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.api.VCByteBuf;

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
    public String getID() {
        return REQUEST_SECRET;
    }

    @Override
    public RequestSecretPacket fromBytes(VCByteBuf buf) {
        compatibilityVersion = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(VCByteBuf buf) {
        buf.writeInt(compatibilityVersion);
    }

}
