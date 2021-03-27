package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.config.ServerConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class InitPacket implements Packet<InitPacket> {

    public static final Identifier SECRET = new Identifier(Voicechat.MODID, "secret");

    private UUID secret;
    private int serverPort;
    private ServerConfig.Codec codec;
    private int mtuSize;
    private double voiceChatDistance;
    private double voiceChatFadeDistance;
    private int keepAlive;

    public InitPacket() {

    }

    public InitPacket(UUID secret, int serverPort, ServerConfig.Codec codec, int mtuSize, double voiceChatDistance, double voiceChatFadeDistance, int keepAlive) {
        this.secret = secret;
        this.serverPort = serverPort;
        this.codec = codec;
        this.mtuSize = mtuSize;
        this.voiceChatDistance = voiceChatDistance;
        this.voiceChatFadeDistance = voiceChatFadeDistance;
        this.keepAlive = keepAlive;
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

    @Override
    public Identifier getID() {
        return SECRET;
    }

    @Override
    public InitPacket fromBytes(PacketByteBuf buf) {
        secret = buf.readUuid();
        serverPort = buf.readInt();
        codec = ServerConfig.Codec.values()[buf.readByte()];
        mtuSize = buf.readInt();
        voiceChatDistance = buf.readDouble();
        voiceChatFadeDistance = buf.readDouble();
        keepAlive = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeUuid(secret);
        buf.writeInt(serverPort);
        buf.writeByte(codec.ordinal());
        buf.writeInt(mtuSize);
        buf.writeDouble(voiceChatDistance);
        buf.writeDouble(voiceChatFadeDistance);
        buf.writeInt(keepAlive);
    }

}
