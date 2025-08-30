package de.maxhenkel.voicechat.sniffer;

import de.maxhenkel.voicechat.VoiceProxy;
import de.maxhenkel.voicechat.util.ByteBufferWrapper;

import java.nio.ByteBuffer;
import java.util.UUID;

public class SniffedSecretPacket {

    protected int compatibilityVersion;

    protected byte[] secret;
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

    public static SniffedSecretPacket fromBytes(ByteBuffer buffer, int compatibilityVersion) throws IncompatibleVoiceChatException {
        if (compatibilityVersion < 10) {
            throw new IncompatibleVoiceChatException(String.format("Client has an outdated voice chat compatibility version (%s)", compatibilityVersion));
        }
        if (compatibilityVersion == 19) {
            throw new IncompatibleVoiceChatException(String.format("Client has an unsupported voice chat compatibility version (%s)", compatibilityVersion));
        }
        if (compatibilityVersion > VoiceProxy.COMPATIBILITY_VERSION) {
            throw new IncompatibleVoiceChatException(String.format("Client has a newer voice chat compatibility version (%s)", compatibilityVersion));
        }
        ByteBufferWrapper buf = new ByteBufferWrapper(buffer);
        SniffedSecretPacket packet = new SniffedSecretPacket();

        packet.secret = new byte[16];
        buf.readBytes(packet.secret);
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
        int size = secret.length + 4 + 16 + 1 + 4 + 8 + 4 + 1 + maxHostSize + 1;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        ByteBufferWrapper buf = new ByteBufferWrapper(buffer);
        buf.writeBytes(secret);
        buf.writeInt(serverPort);
        buf.writeUUID(playerUUID);
        buf.writeByte(codec);
        buf.writeInt(mtuSize);
        buf.writeDouble(voiceChatDistance);
        buf.writeInt(keepAlive);
        buf.writeBoolean(groupsEnabled);
        buf.writeUtf(voiceHost, 32767);
        buf.writeBoolean(allowRecording);

        return ByteBuffer.wrap(buf.toBytes());
    }

    public int getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public int getServerPort() {
        return serverPort;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
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
