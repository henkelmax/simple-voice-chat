package de.maxhenkel.voicechat.net;

import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class InitPacket {

    private UUID secret;
    private int serverPort;
    private int sampleRate;
    private double voiceChatDistance;
    private double voiceChatFadeDistance;

    public InitPacket(UUID secret, int serverPort, int sampleRate, double voiceChatDistance, double voiceChatFadeDistance) {
        this.secret = secret;
        this.serverPort = serverPort;
        this.sampleRate = sampleRate;
        this.voiceChatDistance = voiceChatDistance;
        this.voiceChatFadeDistance = voiceChatFadeDistance;
    }

    public UUID getSecret() {
        return secret;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public double getVoiceChatDistance() {
        return voiceChatDistance;
    }

    public double getVoiceChatFadeDistance() {
        return voiceChatFadeDistance;
    }

    public static InitPacket fromBytes(PacketByteBuf buf) {
        return new InitPacket(buf.readUuid(), buf.readInt(), buf.readInt(), buf.readDouble(), buf.readDouble());
    }

    public void toBytes(PacketByteBuf buf) {
        buf.writeUuid(secret);
        buf.writeInt(serverPort);
        buf.writeInt(sampleRate);
        buf.writeDouble(voiceChatDistance);
        buf.writeDouble(voiceChatFadeDistance);
    }

}
