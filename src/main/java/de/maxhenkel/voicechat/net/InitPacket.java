package de.maxhenkel.voicechat.net;

import de.maxhenkel.voicechat.config.ServerConfig;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class InitPacket {

    private UUID secret;
    private int serverPort;
    private ServerConfig.Codec codec;
    private int mtuSize;
    private double voiceChatDistance;
    private double voiceChatFadeDistance;
    private int keepAlive;

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

    public static InitPacket fromBytes(PacketByteBuf buf) {
        return new InitPacket(buf.readUuid(), buf.readInt(), ServerConfig.Codec.values()[buf.readByte()], buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

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
