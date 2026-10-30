package de.maxhenkel.voicechat.sniffer;

import de.maxhenkel.voicechat.VoiceProxy;
import de.maxhenkel.voicechat.util.ByteBufferWrapper;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.UUID;

public class SniffedSecretPacket {

    private static final byte AUTHENTICATE_PACKET_ID = 0x5;
    private static final int CBC_IV_SIZE = 16;
    private static final int GCM_IV_SIZE = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    protected int compatibilityVersion;
    protected byte[] secret;
    protected UUID connectionId;
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

        packet.compatibilityVersion = compatibilityVersion;
        packet.secret = new byte[16];
        buf.readBytes(packet.secret);
        // Compatibility version 21 added a random connection ID that replaces the player UUID in voice chat packets
        if (compatibilityVersion >= 21) {
            packet.connectionId = buf.readUUID();
        }
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
        int size = secret.length + 16 + 4 + 16 + 1 + 4 + 8 + 4 + 1 + maxHostSize + 1;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        ByteBufferWrapper buf = new ByteBufferWrapper(buffer);
        buf.writeBytes(secret);
        if (connectionId != null) {
            buf.writeUUID(connectionId);
        }
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
     * @return the ID in the header of the players voice chat packets, which is the player UUID before compatibility version 21
     */
    public UUID getConnectionId() {
        return connectionId != null ? connectionId : playerUUID;
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

    /**
     * Checks if the UDP packet is from the player.
     * Since compatibility version 21, the random connection ID is only known to the player, the backend server and the proxy, so it proves this on its own.
     * Before that, the connection ID is the public player UUID, so the packet has to be encrypted with the users secret.
     *
     * @param buffer the voice chat packet, offset right after the connection ID
     * @return if the packet authenticates the player
     */
    public boolean isLegitimateAuthPacket(ByteBuffer buffer) {
        if (connectionId != null) {
            return true;
        }
        try {
            ByteBufferWrapper buf = new ByteBufferWrapper(buffer);
            int payloadLength = buf.readVarInt();
            if (payloadLength < 0 || payloadLength > buffer.remaining()) {
                return false;
            }
            ByteBufferWrapper payload = new ByteBufferWrapper(ByteBuffer.wrap(decrypt(buf.readByteArray(payloadLength))));
            if (payload.readByte() != AUTHENTICATE_PACKET_ID || !playerUUID.equals(payload.readUUID())) {
                return false;
            }
            byte[] authenticationSecret = new byte[secret.length];
            payload.readBytes(authenticationSecret);
            return MessageDigest.isEqual(secret, authenticationSecret);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] decrypt(byte[] payload) throws GeneralSecurityException {
        SecretKeySpec key = new SecretKeySpec(secret, "AES");
        // Compatibility version 19 switched from AES/CBC to AES/GCM
        if (compatibilityVersion < 19) {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(payload, 0, CBC_IV_SIZE));
            return cipher.doFinal(payload, CBC_IV_SIZE, payload.length - CBC_IV_SIZE);
        }
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, payload, 0, GCM_IV_SIZE));
        return cipher.doFinal(payload, GCM_IV_SIZE, payload.length - GCM_IV_SIZE);
    }
}
