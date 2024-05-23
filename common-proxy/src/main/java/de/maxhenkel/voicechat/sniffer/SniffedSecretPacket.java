package de.maxhenkel.voicechat.sniffer;

import de.maxhenkel.voicechat.VoiceProxy;
import de.maxhenkel.voicechat.util.ByteBufferWrapper;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class SniffedSecretPacket {

    protected UUID secret;
    protected int serverPort;
    protected UUID playerUUID;
    protected byte codec;
    protected int mtuSize;
    protected double voiceChatDistance;
    protected int keepAlive;
    protected boolean groupsEnabled;
    protected String voiceHost;
    protected boolean allowRecording;

    protected SniffedSecretPacket() {

    }

    public static SniffedSecretPacket fromBytes(ByteBuffer buffer) {
        ByteBufferWrapper buf = new ByteBufferWrapper(buffer);
        SniffedSecretPacket packet = new SniffedSecretPacket();
        packet.secret = buf.readUUID();
        packet.serverPort = buf.readInt();
        packet.playerUUID = buf.readUUID();
        packet.codec = buf.readByte();
        packet.mtuSize = buf.readInt();
        packet.voiceChatDistance = buf.readDouble();
        packet.keepAlive = buf.readInt();
        packet.groupsEnabled = buf.readBoolean();
        packet.voiceHost = buf.readUtf(32767);
        packet.allowRecording = buf.readBoolean();
        return packet;
    }

    public ByteBuffer toBytes() {
        int maxHostSize = voiceHost.length() * 4 + 5;
        int size = 16 + 4 + 16 + 1 + 4 + 8 + 4 + 1 + maxHostSize + 1;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        ByteBufferWrapper buf = new ByteBufferWrapper(buffer);
        buf.writeUUID(secret);
        buf.writeInt(serverPort);
        buf.writeUUID(playerUUID);
        buf.writeByte(codec);
        buf.writeInt(mtuSize);
        buf.writeDouble(voiceChatDistance);
        buf.writeInt(keepAlive);
        buf.writeBoolean(groupsEnabled);
        buf.writeUtf(voiceHost, 32767);
        buf.writeBoolean(allowRecording);
        return ByteBuffer.wrap(Arrays.copyOfRange(buffer.array(), 0, buffer.position()));
    }

    public UUID getSecret() {
        return secret;
    }

    public int getServerPort() {
        return serverPort;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public byte getCodec() {
        return codec;
    }

    public int getMtuSize() {
        return mtuSize;
    }

    public double getVoiceChatDistance() {
        return voiceChatDistance;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public boolean isGroupsEnabled() {
        return groupsEnabled;
    }

    public String getVoiceHost() {
        return voiceHost;
    }

    public boolean isAllowRecording() {
        return allowRecording;
    }

    /**
     * Modifies the packet to use the proxy port and clears the voice host.
     * If a voice host is present on the proxy, this value will be used.
     *
     * @param voiceProxy the proxy
     * @return the modified packet
     */
    public ByteBuffer patch(VoiceProxy voiceProxy) {
        serverPort = voiceProxy.getPort();
        voiceHost = voiceProxy.getConfig().voiceHost.get();
        return toBytes();
    }
}
