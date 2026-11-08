package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.net.*;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.common.Secret;
import de.maxhenkel.voicechat.voice.common.Utils;
import de.maxhenkel.voicechat.voice.server.ConnectionSecret;
import de.maxhenkel.voicechat.voice.server.Server;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.UUID;

/**
 * For compatibility version 20 (voicechat 2.6.x).
 */
public class LegacyProtocolV20 implements LegacyProtocol {

    private static final byte AUTHENTICATE_PACKET_ID = 0x5;
    private static final byte AUTHENTICATE_ACK_PACKET_ID = 0x6;
    private static final byte CONNECTION_CHECK_PACKET_ID = 0x9;

    @Override
    public Packet<?> translateOutgoing(Packet<?> packet) {
        if (packet instanceof SecretPacket) {
            return new LegacyPacket(packet.getIdentifier(), buf -> writeSecret(buf, (SecretPacket) packet));
        } else if (packet instanceof PlayerStatePacket) {
            return new LegacyPacket(packet.getIdentifier(), buf -> writePlayerState(buf, ((PlayerStatePacket) packet).getPlayerState()));
        } else if (packet instanceof PlayerStatesPacket) {
            return new LegacyPacket(packet.getIdentifier(), buf -> writePlayerStates(buf, ((PlayerStatesPacket) packet).getPlayerStates()));
        } else if (packet instanceof AddCategoryPacket) {
            return new LegacyPacket(packet.getIdentifier(), buf -> writeCategory(buf, ((AddCategoryPacket) packet).getCategory()));
        }
        return packet;
    }

    @Override
    public <T extends Packet<T>> T translateIncoming(T packet) {
        return packet;
    }

    @Override
    @Nullable
    public UUID getPlayerUuid(byte[] packet) {
        PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(packet));
        if (buf.readByte() != NetworkMessage.MAGIC_BYTE) {
            return null;
        }
        // Player UUID instead of connection ID
        return buf.readUniqueId();
    }

    @Override
    public byte[] translateIncomingVoicePacket(Server server, ConnectionSecret connectionSecret, SocketAddress address, byte[] packet) throws Exception {
        PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(packet));
        buf.readByte();
        buf.readUniqueId();
        Secret secret = connectionSecret.getSecret();
        byte[] payload = buf.readByteArray(Utils.MAX_VOICE_CHAT_PACKET_SIZE);
        byte[] data = secret.decrypt(payload);
        if (data[0] == AUTHENTICATE_PACKET_ID) {
            // Drop the player UUID and secret, decryption succeeding already proves it's the player
            payload = secret.encrypt(new byte[]{AUTHENTICATE_PACKET_ID});
        } else if (data[0] == CONNECTION_CHECK_PACKET_ID) {
            // Fake a nonce so the server accepts the connection
            byte[] nonce = server.createNonce(connectionSecret.getConnectionId(), address);
            payload = secret.encrypt(ByteBuffer.allocate(data.length + nonce.length).put(data).put(nonce).array());
        }

        PacketBuffer translated = new PacketBuffer(Unpooled.buffer());
        translated.writeByte(NetworkMessage.MAGIC_BYTE);
        translated.writeUniqueId(connectionSecret.getConnectionId());
        translated.writeByteArray(payload);
        return toByteArray(translated);
    }

    @Override
    public byte[] translateOutgoingVoicePacket(ConnectionSecret connectionSecret, byte[] packet) throws Exception {
        PacketBuffer buf = new PacketBuffer(Unpooled.wrappedBuffer(packet));
        buf.readByte();
        byte[] data = connectionSecret.getSecret().decrypt(buf.readByteArray(Utils.MAX_VOICE_CHAT_PACKET_SIZE));
        if (data[0] != AUTHENTICATE_ACK_PACKET_ID) {
            return packet;
        }

        // Drop the nonce from the auth ack packet
        PacketBuffer translated = new PacketBuffer(Unpooled.buffer());
        translated.writeByte(NetworkMessage.MAGIC_BYTE);
        translated.writeByteArray(connectionSecret.getSecret().encrypt(new byte[]{AUTHENTICATE_ACK_PACKET_ID}));
        return toByteArray(translated);
    }

    private static byte[] toByteArray(PacketBuffer buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    private static void writeSecret(PacketBuffer buf, SecretPacket packet) {
        packet.getSecret().toBytes(buf);
        buf.writeInt(packet.getServerPort());
        buf.writeUniqueId(packet.getPlayerUUID());
        buf.writeByte(packet.getCodec().ordinal());
        buf.writeInt(packet.getMtuSize());
        buf.writeDouble(packet.getVoiceChatDistance());
        buf.writeInt(packet.getKeepAlive());
        buf.writeBoolean(packet.groupsEnabled());
        buf.writeString(packet.getVoiceHost());
        buf.writeBoolean(packet.allowRecording());
    }

    private static void writePlayerStates(PacketBuffer buf, Collection<PlayerState> states) {
        buf.writeInt(states.size());
        for (PlayerState state : states) {
            writePlayerState(buf, state);
        }
    }

    private static void writePlayerState(PacketBuffer buf, PlayerState state) {
        buf.writeBoolean(state.isDisabledOrForceDisabled());
        buf.writeBoolean(state.isDisconnected());
        buf.writeUniqueId(state.getUuid());
        buf.writeString(state.getName());
        UUID group = state.getGroup();
        buf.writeBoolean(group != null);
        if (group != null) {
            buf.writeUniqueId(group);
        }
    }

    private static void writeCategory(PacketBuffer buf, VolumeCategoryImpl category) {
        buf.writeString(category.getId());
        buf.writeString(category.getName());
        writeOptionalString(buf, category.getNameTranslationKey());
        writeOptionalString(buf, category.getDescription());
        writeOptionalString(buf, category.getDescriptionTranslationKey());
        int[][] icon = category.getIcon();
        buf.writeBoolean(icon != null);
        if (icon != null) {
            for (int[] column : icon) {
                for (int pixel : column) {
                    buf.writeInt(pixel);
                }
            }
        }
    }

    private static void writeOptionalString(PacketBuffer buf, @Nullable String string) {
        buf.writeBoolean(string != null);
        if (string != null) {
            buf.writeString(string);
        }
    }

}
