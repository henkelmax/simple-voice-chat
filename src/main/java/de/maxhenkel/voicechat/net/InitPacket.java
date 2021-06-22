package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class InitPacket implements Packet<InitPacket> {

    public static final ResourceLocation SECRET = new ResourceLocation(Voicechat.MODID, "secret");

    private UUID secret;
    private int serverPort;
    private ServerConfig.Codec codec;
    private int mtuSize;
    private double voiceChatDistance;
    private double voiceChatFadeDistance;
    private int keepAlive;
    private boolean groupsEnabled;
    private String voiceHost;


    public InitPacket() {

    }

    public InitPacket(UUID secret, int serverPort, ServerConfig.Codec codec, int mtuSize, double voiceChatDistance, double voiceChatFadeDistance, int keepAlive, boolean groupsEnabled, String voiceHost) {
        this.secret = secret;
        this.serverPort = serverPort;
        this.codec = codec;
        this.mtuSize = mtuSize;
        this.voiceChatDistance = voiceChatDistance;
        this.voiceChatFadeDistance = voiceChatFadeDistance;
        this.keepAlive = keepAlive;
        this.groupsEnabled = groupsEnabled;
        this.voiceHost = voiceHost;
    }

    public UUID getSecret() {
        return secret;
    }

    public int getServerPort() {
        return serverPort;
    }

    public ServerConfig.Codec getCodec() {
        return codec;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public double getVoiceChatDistance() {
        return voiceChatDistance;
    }

    public double getVoiceChatFadeDistance() {
        return voiceChatFadeDistance;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean groupsEnabled() {
        return groupsEnabled;
    }

    public String getVoiceHost() { return voiceHost; }

    @Override
    public ResourceLocation getID() {
        return SECRET;
    }

    @Override
    public InitPacket fromBytes(FriendlyByteBuf buf) {
        secret = buf.readUUID();
        serverPort = buf.readInt();
        codec = ServerConfig.Codec.values()[buf.readByte()];
        mtuSize = buf.readInt();
        voiceChatDistance = buf.readDouble();
        voiceChatFadeDistance = buf.readDouble();
        keepAlive = buf.readInt();
        groupsEnabled = buf.readBoolean();
        voiceHost = buf.readUtf();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(secret);
        buf.writeInt(serverPort);
        buf.writeByte(codec.ordinal());
        buf.writeInt(mtuSize);
        buf.writeDouble(voiceChatDistance);
        buf.writeDouble(voiceChatFadeDistance);
        buf.writeInt(keepAlive);
        buf.writeBoolean(groupsEnabled);
        buf.writeUtf(voiceHost);
    }

}
