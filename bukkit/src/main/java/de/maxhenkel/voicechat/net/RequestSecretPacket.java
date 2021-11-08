package de.maxhenkel.voicechat.net;

import com.comphenix.protocol.wrappers.MinecraftKey;
import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;

public class RequestSecretPacket implements Packet<RequestSecretPacket> {

    public static final MinecraftKey REQUEST_SECRET = new MinecraftKey(Voicechat.MODID, "request_secret");

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
    public MinecraftKey getID() {
        return REQUEST_SECRET;
    }

    @Override
    public RequestSecretPacket fromBytes(FriendlyByteBuf buf) {
        compatibilityVersion = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(compatibilityVersion);
    }

}
