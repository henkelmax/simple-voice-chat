package de.maxhenkel.voicechat.legacy;

import de.maxhenkel.voicechat.Voicechat;
import de.maxhenkel.voicechat.net.*;
import de.maxhenkel.voicechat.plugins.impl.VolumeCategoryImpl;
import de.maxhenkel.voicechat.voice.common.Secret;
import de.maxhenkel.voicechat.voice.common.Utils;
import de.maxhenkel.voicechat.voice.server.PlayerStateManager;
import de.maxhenkel.voicechat.voice.server.Server;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;

/**
 * For compatibility version 18 (voicechat 2.5.x).
 */
public class LegacyProtocolV18 extends LegacyProtocolV20 {

    private static final String CIPHER = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE_BYTES = 16;

    @Override
    @Nullable
    public Packet<?> translateOutgoing(Packet<?> packet) {
        if (packet instanceof SecretPacket) {
            return new LegacyPacket(packet.getIdentifier(), buf -> writeSecret(buf, (SecretPacket) packet));
        } else if (packet instanceof AddCategoryPacket) {
            return new LegacyPacket(packet.getIdentifier(), buf -> writeCategory(buf, ((AddCategoryPacket) packet).getCategory()));
        } else if (packet instanceof PlayerStatePacket) {
            return new LegacyPlayerStatePacket(((PlayerStatePacket) packet).getPlayerState());
        } else if (packet instanceof PlayerStatesPacket) {
            return new LegacyPlayerStatesPacket(((PlayerStatesPacket) packet).getPlayerStates());
        } else if (packet instanceof RemovePlayerStatePacket) {
            // This protocol removes players by marking them as disconnected
            Server server = Voicechat.SERVER.getServer();
            if (server == null) {
                return null;
            }
            EntityPlayerMP player = server.getServer().getPlayerList().getPlayerByUUID(((RemovePlayerStatePacket) packet).getId());
            if (player == null) {
                return null;
            }
            return new LegacyPlayerStatePacket(PlayerStateManager.defaultDisconnectedState(player));
        }
        return super.translateOutgoing(packet);
    }

    @Override
    protected byte[] decrypt(Secret secret, byte[] payload) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, secret.getKeySpec(), new IvParameterSpec(payload, 0, IV_SIZE_BYTES));
        return cipher.doFinal(payload, IV_SIZE_BYTES, payload.length - IV_SIZE_BYTES);
    }

    @Override
    protected byte[] encrypt(Secret secret, byte[] data) throws Exception {
        byte[] iv = Utils.generateSecureRandomBytes(IV_SIZE_BYTES);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, secret.getKeySpec(), new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(data);
        return ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
    }

    /**
     * This protocol doesn't support voice hosts with just a port, so the port is sent as the server port instead.
     */
    private static void writeSecret(PacketBuffer buf, SecretPacket packet) {
        int serverPort = packet.getServerPort();
        String voiceHost = packet.getVoiceHost();
        try {
            int voiceHostPort = Integer.parseInt(voiceHost);
            voiceHost = "";
            if (voiceHostPort > 0 && voiceHostPort <= 65535) {
                serverPort = voiceHostPort;
            }
        } catch (NumberFormatException ignored) {
        }
        writeSecret(buf, packet, serverPort, voiceHost);
    }

    private static void writeCategory(PacketBuffer buf, VolumeCategoryImpl category) {
        buf.writeString(category.getId());
        buf.writeString(category.getName());
        writeOptionalString(buf, category.getDescription());
        writeIcon(buf, category.getIcon());
    }

}
