package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.debug.CooldownTimer;
import de.maxhenkel.voicechat.net.*;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.common.AuthenticateAckPacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.common.Secret;
import de.maxhenkel.voicechat.voice.common.Utils;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.ConnectionSecret;
import de.maxhenkel.voicechat.voice.server.Server;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.UUID;

/**
 * For compatibility version 20 (voicechat 2.6.x).
 */
public class LegacyProtocolV20 implements LegacyProtocol {

    private static final byte MIC_PACKET_ID = 0x1;
    private static final byte AUTHENTICATE_PACKET_ID = 0x5;
    private static final byte KEEP_ALIVE_PACKET_ID = 0x8;
    private static final byte CONNECTION_CHECK_PACKET_ID = 0x9;

    @Override
    public Packet<?> translateOutgoing(Packet<?> packet) {
        if (packet instanceof SecretPacket) {
            SecretPacket secretPacket = (SecretPacket) packet;
            return new LegacyPacket(packet.getIdentifier(), buf -> writeSecret(buf, secretPacket, secretPacket.getServerPort(), secretPacket.getVoiceHost()));
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
        byte[] data = decrypt(connectionSecret.getSecret(), buf.readByteArray(Utils.MAX_VOICE_CHAT_PACKET_SIZE));
        notifyForcedState(server, connectionSecret.getPlayerUUID(), data[0]);
        if (data[0] == AUTHENTICATE_PACKET_ID) {
            // Drop the player UUID and secret, decryption succeeding already proves it's the player
            data = new byte[]{AUTHENTICATE_PACKET_ID};
        } else if (data[0] == CONNECTION_CHECK_PACKET_ID) {
            // Fake a nonce so the server accepts the connection
            byte[] nonce = server.createNonce(connectionSecret.getConnectionId(), address);
            data = ByteBuffer.allocate(data.length + nonce.length).put(data).put(nonce).array();
        }

        PacketBuffer translated = new PacketBuffer(Unpooled.buffer());
        translated.writeByte(NetworkMessage.MAGIC_BYTE);
        translated.writeUniqueId(connectionSecret.getConnectionId());
        translated.writeByteArray(connectionSecret.getSecret().encrypt(data));
        return toByteArray(translated);
    }

    @Override
    public byte[] translateOutgoingVoicePacket(ClientConnection connection, NetworkMessage message) throws Exception {
        if (message.getPacket() instanceof AuthenticateAckPacket) {
            // This protocol has no nonce
            message = new NetworkMessage(new AuthenticateAckPacket(new byte[0]));
        }
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeByte(NetworkMessage.MAGIC_BYTE);
        buf.writeByteArray(encrypt(connection.getConnectionSecret().getSecret(), message.encode()));
        return toByteArray(buf);
    }

    protected byte[] decrypt(Secret secret, byte[] payload) throws Exception {
        return secret.decrypt(payload);
    }

    protected byte[] encrypt(Secret secret, byte[] data) throws Exception {
        return secret.encrypt(data);
    }

    private static void notifyForcedState(Server server, UUID playerUuid, byte packetId) {
        PlayerState state = server.getPlayerStateManager().getState(playerUuid);
        if (state == null) {
            return;
        }
        if (packetId == MIC_PACKET_ID && state.isForceMuted()) {
            sendOverlayMessage(server, playerUuid, "message.voicechat.no_speak_permission");
        } else if (packetId == KEEP_ALIVE_PACKET_ID && state.isForceDisabled()) {
            sendOverlayMessage(server, playerUuid, "message.voicechat.no_listen_permission");
        }
    }

    private static void sendOverlayMessage(Server server, UUID playerUuid, String translationKey) {
        CooldownTimer.run(translationKey + playerUuid, 30_000L, () -> {
            EntityPlayerMP player = server.getServer().getPlayerList().getPlayerByUUID(playerUuid);
            if (player != null) {
                player.sendStatusMessage(new TextComponentTranslation(translationKey), true);
            }
        });
    }

    private static byte[] toByteArray(PacketBuffer buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    protected static void writeSecret(PacketBuffer buf, SecretPacket packet, int serverPort, String voiceHost) {
        packet.getSecret().toBytes(buf);
        buf.writeInt(serverPort);
        buf.writeUniqueId(packet.getPlayerUUID());
        buf.writeByte(packet.getCodec().ordinal());
        buf.writeInt(packet.getMtuSize());
        buf.writeDouble(packet.getVoiceChatDistance());
        buf.writeInt(packet.getKeepAlive());
        buf.writeBoolean(packet.groupsEnabled());
        buf.writeString(voiceHost);
        buf.writeBoolean(packet.allowRecording());
    }

    protected static void writePlayerStates(PacketBuffer buf, Collection<PlayerState> states) {
        buf.writeInt(states.size());
        for (PlayerState state : states) {
            writePlayerState(buf, state);
        }
    }

    protected static void writePlayerState(PacketBuffer buf, PlayerState state) {
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
        writeIcon(buf, category.getIcon());
    }

    protected static void writeIcon(PacketBuffer buf, @Nullable int[][] icon) {
        buf.writeBoolean(icon != null);
        if (icon != null) {
            for (int[] column : icon) {
                for (int pixel : column) {
                    buf.writeInt(pixel);
                }
            }
        }
    }

    protected static void writeOptionalString(PacketBuffer buf, @Nullable String string) {
        buf.writeBoolean(string != null);
        if (string != null) {
            buf.writeString(string);
        }
    }

}
