package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.util.FriendlyByteBuf;
import de.maxhenkel.voicechat.util.ResourceLocation;
import org.bukkit.entity.Player;

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
    public ResourceLocation getID() {
        return REQUEST_SECRET;
    }

    @Override
    public void onPacket(Player player) {
        Voicechat.SERVER.onRequestSecretPacket(player, this);
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
