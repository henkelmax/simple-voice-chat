package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class RequestSecretPacket implements Packet<RequestSecretPacket> {

    public static final CustomPacketPayload.Type<RequestSecretPacket> REQUEST_SECRET = new CustomPacketPayload.Type<>(new ResourceLocation(Voicechat.MODID, "request_secret"));

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
    public RequestSecretPacket fromBytes(RegistryFriendlyByteBuf buf) {
        compatibilityVersion = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeInt(compatibilityVersion);
    }

    @Override
    public Type<RequestSecretPacket> type() {
        return REQUEST_SECRET;
    }

}
